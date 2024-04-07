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
import cloud.wangyongjun.vxmq.event.Event;
import cloud.wangyongjun.vxmq.event.EventService;
import cloud.wangyongjun.vxmq.event.mqtt.MqttPublishOutboundAckedEvent;
import cloud.wangyongjun.vxmq.service.msg.MsgService;
import cloud.wangyongjun.vxmq.service.msg.OutboundQos2Pub;
import cloud.wangyongjun.vxmq.service.msg.OutboundQos2Rel;
import cloud.wangyongjun.vxmq.service.session.SessionService;
import io.netty.handler.codec.mqtt.MqttProperties;
import io.netty.handler.codec.mqtt.MqttVersion;
import io.smallrye.mutiny.Uni;
import io.vertx.core.json.JsonObject;
import io.vertx.mqtt.messages.codes.MqttPubRelReasonCode;
import io.vertx.mutiny.core.Vertx;
import io.vertx.mutiny.mqtt.MqttEndpoint;
import io.vertx.mutiny.mqtt.messages.MqttPubRecMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.function.Consumer;

/**
 * This handler is called when the server received a PUBREC package, and this communication only happened with QoS 2 and the direction is from server to client.
 * <br/>
 * SERVER  --PUBLISH QoS 2-->  Client<br/>
 * <br/>
 * SERVER  <-----PUBREC------  Client<br/>
 * <br/>
 * SERVER  ------PUBREL----->  Client<br/>
 * <br/>
 * SERVER  <-----PUBCOMP-----  Client<br/>
 * <br/>
 */
public class MqttPublishReceivedMessageHandler implements Consumer<MqttPubRecMessage> {

  private static final Logger LOGGER = LoggerFactory.getLogger(MqttPublishReceivedMessageHandler.class);

  private final MqttEndpoint mqttEndpoint;
  private final SessionService sessionService;
  private final MsgService msgService;
  private final EventService eventService;
  private final Vertx vertx;

  public MqttPublishReceivedMessageHandler(MqttEndpoint mqttEndpoint, SessionService sessionService, MsgService msgService, EventService eventService, Vertx vertx) {
    this.mqttEndpoint = mqttEndpoint;
    this.sessionService = sessionService;
    this.msgService = msgService;
    this.eventService = eventService;
    this.vertx = vertx;
  }

  @Override
  public void accept(MqttPubRecMessage mqttPubRecMessage) {
    if (LOGGER.isDebugEnabled()){
      LOGGER.debug("PUBREC from {}: {}", mqttEndpoint.clientIdentifier(), pubRecInfo(mqttPubRecMessage));
    }
    MqttProperties pubRelProperties = new MqttProperties();
    sessionService.getSession(mqttEndpoint.clientIdentifier())
      .onItem().transformToUni(session -> msgService.getAndRemoveOutboundQos2Pub(session.getSessionId(), mqttPubRecMessage.messageId())
        .onItem().transformToUni(outboundQos2Pub -> {
          if (mqttEndpoint.protocolVersion() <= MqttVersion.MQTT_3_1_1.protocolLevel()) {
            if (outboundQos2Pub == null) {
              LOGGER.warn("PUBREC from {} without having related PUBLISH packet", mqttEndpoint.clientIdentifier());
            }
            mqttEndpoint.publishRelease(mqttPubRecMessage.messageId());
          } else {
            if (outboundQos2Pub == null) {
              LOGGER.warn("PUBREC from {} without having related PUBLISH packet", mqttEndpoint.clientIdentifier());
              mqttEndpoint.publishRelease(mqttPubRecMessage.messageId(), MqttPubRelReasonCode.PACKET_IDENTIFIER_NOT_FOUND, pubRelProperties);
            } else {
              mqttEndpoint.publishRelease(mqttPubRecMessage.messageId(), MqttPubRelReasonCode.SUCCESS, pubRelProperties);
            }
          }
          return Uni.createFrom().voidItem()
            .onItem().transformToUni(v -> {
              if (outboundQos2Pub != null){
                return publishEvent(outboundQos2Pub);
              }else {
                return Uni.createFrom().voidItem();
              }
            });
        })
        .onItem().transformToUni(v -> msgService.saveOutboundQos2Rel(new OutboundQos2Rel(session.getSessionId(), mqttEndpoint.clientIdentifier(), mqttPubRecMessage.messageId(), Instant.now().toEpochMilli()))))
      .subscribe().with(ConsumerUtil.nothingToDo(), t -> LOGGER.error("Error occurred when processing PUBREC from {}", mqttEndpoint.clientIdentifier(), t));
  }

  private String pubRecInfo(MqttPubRecMessage mqttPubRecMessage) {
    JsonObject jsonObject = new JsonObject();
    jsonObject.put("messageId", mqttPubRecMessage.messageId());
    jsonObject.put("code", mqttPubRecMessage.code());
    jsonObject.put("properties", MqttPropertiesUtil.encode(mqttPubRecMessage.properties()));
    return jsonObject.toString();
  }

  private Uni<Void> publishEvent(OutboundQos2Pub outboundQos2Pub){
    Event event = new MqttPublishOutboundAckedEvent(Instant.now().toEpochMilli(),
      VertxUtil.getNodeId(vertx), outboundQos2Pub.getSessionId(), outboundQos2Pub.getClientId(),
      outboundQos2Pub.getMessageId(), outboundQos2Pub.getTopic(), outboundQos2Pub.getQos(),
      outboundQos2Pub.getPayload(), outboundQos2Pub.isDup(), outboundQos2Pub.isRetain());
    if (LOGGER.isDebugEnabled()){
      LOGGER.debug("Publishing event: {}, ", event.toJson());
    }
    return eventService.publishEvent(event);
  }

}
