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

package io.github.jpforevers.vxmq.mqtt.handler;

import io.github.jpforevers.vxmq.assist.ConsumerUtil;
import io.github.jpforevers.vxmq.assist.MqttPropertiesUtil;
import io.github.jpforevers.vxmq.assist.VertxUtil;
import io.github.jpforevers.vxmq.event.Event;
import io.github.jpforevers.vxmq.event.EventService;
import io.github.jpforevers.vxmq.event.mqtt.MqttPublishOutboundAckedEvent;
import io.github.jpforevers.vxmq.service.flow.FlowControlService;
import io.github.jpforevers.vxmq.service.msg.MsgService;
import io.github.jpforevers.vxmq.service.msg.OutboundQos2Pub;
import io.github.jpforevers.vxmq.service.msg.OutboundQos2Rel;
import io.github.jpforevers.vxmq.service.session.Session;
import io.github.jpforevers.vxmq.service.session.SessionService;
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
  private final FlowControlService flowControlService;
  private final Vertx vertx;

  public MqttPublishReceivedMessageHandler(MqttEndpoint mqttEndpoint, SessionService sessionService, MsgService msgService, EventService eventService, FlowControlService flowControlService, Vertx vertx) {
    this.mqttEndpoint = mqttEndpoint;
    this.sessionService = sessionService;
    this.msgService = msgService;
    this.eventService = eventService;
    this.flowControlService = flowControlService;
    this.vertx = vertx;
  }

  @Override
  public void accept(MqttPubRecMessage mqttPubRecMessage) {
    if (LOGGER.isDebugEnabled()){
      LOGGER.debug("PUBREC from {}: {}", mqttEndpoint.clientIdentifier(), pubRecInfo(mqttPubRecMessage));
    }

    // From https://docs.oasis-open.org/mqtt/mqtt/v5.0/os/mqtt-v5.0-os.html#_Toc3901120: The Server MUST NOT send more than Receive Maximum QoS 1 and QoS 2 PUBLISH packets for which it has not received PUBACK, PUBCOMP, or PUBREC with a Reason Code of 128 or greater from the Client [MQTT-3.3.4-9]. If it receives more than Receive Maximum QoS 1 and QoS 2 PUBLISH packets where it has not sent a PUBACK or PUBCOMP in response, the Client uses DISCONNECT with Reason Code 0x93 (Receive Maximum exceeded) as described in section 4.13 Handling errors.
    // So, The MQTT broker should decrement the outbound reception number when received a PUBREC message with a Reason Code of 128 or greater.
    if (mqttPubRecMessage.code().isError()) {
      flowControlService.decrementOutboundReceive(mqttEndpoint.clientIdentifier());
    }

    MqttProperties pubRelProperties = new MqttProperties();
    sessionService.getSessionByFields(mqttEndpoint.clientIdentifier(), new Session.Field[]{Session.Field.sessionId})
      .onItem().transformToUni(sessionFields -> msgService.getAndRemoveOutboundQos2Pub((String) sessionFields.get(Session.Field.sessionId), mqttPubRecMessage.messageId())
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
        .onItem().transformToUni(v -> msgService.saveOutboundQos2Rel(new OutboundQos2Rel((String) sessionFields.get(Session.Field.sessionId), mqttEndpoint.clientIdentifier(), mqttPubRecMessage.messageId(), Instant.now().toEpochMilli()))))
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
