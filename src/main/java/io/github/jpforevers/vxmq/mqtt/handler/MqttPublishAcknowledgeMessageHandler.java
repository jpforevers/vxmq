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
import io.github.jpforevers.vxmq.service.msg.OutboundQos1Pub;
import io.github.jpforevers.vxmq.service.session.Session;
import io.github.jpforevers.vxmq.service.session.SessionService;
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
  private final FlowControlService flowControlService;
  private final Vertx vertx;

  public MqttPublishAcknowledgeMessageHandler(MqttEndpoint mqttEndpoint, SessionService sessionService, MsgService msgService,
                                              EventService eventService, FlowControlService flowControlService,
                                              Vertx vertx) {
    this.mqttEndpoint = mqttEndpoint;
    this.sessionService = sessionService;
    this.msgService = msgService;
    this.eventService = eventService;
    this.flowControlService = flowControlService;
    this.vertx = vertx;
  }

  @Override
  public void accept(MqttPubAckMessage mqttPubAckMessage) {
    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("PUBACK from {}: {}", mqttEndpoint.clientIdentifier(), pubAckInfo(mqttPubAckMessage));
    }

    // From https://docs.oasis-open.org/mqtt/mqtt/v5.0/os/mqtt-v5.0-os.html#_Toc3901120: The Server MUST NOT send more than Receive Maximum QoS 1 and QoS 2 PUBLISH packets for which it has not received PUBACK, PUBCOMP, or PUBREC with a Reason Code of 128 or greater from the Client [MQTT-3.3.4-9]. If it receives more than Receive Maximum QoS 1 and QoS 2 PUBLISH packets where it has not sent a PUBACK or PUBCOMP in response, the Client uses DISCONNECT with Reason Code 0x93 (Receive Maximum exceeded) as described in section 4.13 Handling errors.
    // So, The MQTT broker should decrement the outbound reception number after received a PUBACK message.
    flowControlService.decrementOutboundReceive(mqttEndpoint.clientIdentifier());

    sessionService.getSessionByFields(mqttEndpoint.clientIdentifier(), new Session.Field[]{Session.Field.sessionId})
      .onItem().transformToUni(sessionFields -> msgService.getAndRemoveOutboundQos1Pub((String) sessionFields.get(Session.Field.sessionId), mqttPubAckMessage.messageId()))
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
