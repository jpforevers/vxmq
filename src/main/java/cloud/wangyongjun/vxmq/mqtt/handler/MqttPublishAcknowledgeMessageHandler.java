/*
 * Copyright 2018-present 王用军
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package cloud.wangyongjun.vxmq.mqtt.handler;

import cloud.wangyongjun.vxmq.assist.ConsumerUtil;
import cloud.wangyongjun.vxmq.assist.MqttPropertiesUtil;
import cloud.wangyongjun.vxmq.assist.VertxUtil;
import cloud.wangyongjun.vxmq.event.EventService;
import cloud.wangyongjun.vxmq.event.MqttPublishOutboundAckedEvent;
import cloud.wangyongjun.vxmq.service.msg.MsgService;
import cloud.wangyongjun.vxmq.service.session.SessionService;
import io.smallrye.mutiny.Uni;
import io.vertx.core.json.JsonObject;
import io.vertx.mutiny.core.Vertx;
import io.vertx.mutiny.mqtt.MqttEndpoint;
import io.vertx.mutiny.mqtt.messages.MqttPubAckMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.function.Consumer;

/**
 * This handler will be called when the server received a PUBACK package, and this communication only happened with QoS 1 and the direction is from server to client.<br/>
 * <br/>
 * SERVER  --PUBLISH QoS 1-->  Client<br/>
 * <br/>
 * SERVER  <-----PUBACK------  Client<br/>
 * <br/>
 */
public class MqttPublishAcknowledgeMessageHandler implements Consumer<MqttPubAckMessage> {

  private final static Logger LOGGER = LoggerFactory.getLogger(MqttPublishAcknowledgeMessageHandler.class);

  private final MqttEndpoint mqttEndpoint;
  private final SessionService sessionService;
  private final MsgService msgService;
  private final EventService eventService;
  private final Vertx vertx;

  public MqttPublishAcknowledgeMessageHandler(MqttEndpoint mqttEndpoint, SessionService sessionService, MsgService msgService, EventService eventService, Vertx vertx) {
    this.mqttEndpoint = mqttEndpoint;
    this.sessionService = sessionService;
    this.msgService = msgService;
    this.eventService = eventService;
    this.vertx = vertx;
  }

  @Override
  public void accept(MqttPubAckMessage mqttPubAckMessage) {
    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("PUBACK from {}: {}", mqttEndpoint.clientIdentifier(), pubAckInfo(mqttPubAckMessage));
    }
    sessionService.getSession(mqttEndpoint.clientIdentifier())
      .onItem().transformToUni(session -> msgService.getAndRemoveOutboundQos1Pub(session.getSessionId(), mqttPubAckMessage.messageId()))
      .onItem().transformToUni(outboundQos1Pub -> {
        if (outboundQos1Pub == null) {
          LOGGER.warn("PUBACK from {} with messageId {} without having related PUBLISH packet", mqttEndpoint.clientIdentifier(), mqttPubAckMessage.messageId());
          return Uni.createFrom().voidItem();
        } else {
          return eventService.publishEvent(new MqttPublishOutboundAckedEvent(Instant.now().toEpochMilli(),
            VertxUtil.getNodeId(vertx), outboundQos1Pub.getSessionId(), outboundQos1Pub.getClientId(),
            outboundQos1Pub.getMessageId(), outboundQos1Pub.getTopic(), outboundQos1Pub.getQos(),
            outboundQos1Pub.getPayload(), outboundQos1Pub.isDup(), outboundQos1Pub.isRetain()));
        }
      })
      .subscribe().with(ConsumerUtil.nothingToDo(), t -> LOGGER.error("Error occurred when processing PUBACK from {}", mqttEndpoint.clientIdentifier(), t));
  }

  private String pubAckInfo(MqttPubAckMessage mqttPubAckMessage) {
    JsonObject jsonObject = new JsonObject();
    jsonObject.put("messageId", mqttPubAckMessage.messageId());
    jsonObject.put("code", mqttPubAckMessage.code());
    jsonObject.put("properties", MqttPropertiesUtil.encode(mqttPubAckMessage.properties()));
    return jsonObject.toString();
  }

}
