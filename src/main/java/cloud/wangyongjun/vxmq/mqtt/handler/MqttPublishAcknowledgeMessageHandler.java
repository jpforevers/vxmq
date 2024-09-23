/*
 * Copyright (C) 2023-2024 王用军
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package cloud.wangyongjun.vxmq.mqtt.handler;

import cloud.wangyongjun.vxmq.assist.ConsumerUtil;
import cloud.wangyongjun.vxmq.assist.MqttPropertiesUtil;
import cloud.wangyongjun.vxmq.assist.VertxUtil;
import cloud.wangyongjun.vxmq.event.Event;
import cloud.wangyongjun.vxmq.event.EventService;
import cloud.wangyongjun.vxmq.event.mqtt.MqttPublishOutboundAckedEvent;
import cloud.wangyongjun.vxmq.service.msg.MsgService;
import cloud.wangyongjun.vxmq.service.msg.OutboundQos1Pub;
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
          return publishEvent(outboundQos1Pub);
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

  private Uni<Void> publishEvent(OutboundQos1Pub outboundQos1Pub){
    Event event = new MqttPublishOutboundAckedEvent(Instant.now().toEpochMilli(),
      VertxUtil.getNodeId(vertx), outboundQos1Pub.getSessionId(), outboundQos1Pub.getClientId(),
      outboundQos1Pub.getMessageId(), outboundQos1Pub.getTopic(), outboundQos1Pub.getQos(),
      outboundQos1Pub.getPayload(), outboundQos1Pub.isDup(), outboundQos1Pub.isRetain());
    if (LOGGER.isDebugEnabled()){
      LOGGER.debug("Publishing event: {}, ", event.toJson());
    }
    return eventService.publishEvent(event);
  }

}
