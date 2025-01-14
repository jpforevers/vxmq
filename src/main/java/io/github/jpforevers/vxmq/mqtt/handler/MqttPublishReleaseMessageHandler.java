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
import io.github.jpforevers.vxmq.service.composite.CompositeService;
import io.github.jpforevers.vxmq.service.flow.FlowService;
import io.github.jpforevers.vxmq.service.msg.MsgService;
import io.github.jpforevers.vxmq.service.msg.MsgToTopic;
import io.github.jpforevers.vxmq.service.session.Session;
import io.github.jpforevers.vxmq.service.session.SessionService;
import io.netty.handler.codec.mqtt.MqttProperties;
import io.netty.handler.codec.mqtt.MqttVersion;
import io.smallrye.mutiny.Uni;
import io.vertx.core.json.JsonObject;
import io.vertx.mqtt.messages.codes.MqttPubCompReasonCode;
import io.vertx.mutiny.mqtt.MqttEndpoint;
import io.vertx.mutiny.mqtt.messages.MqttPubRelMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Consumer;

/**
 * This handler is called when the server received a PUBREL package, and this communication only happened on QoS 2 and the direction is from client to server.<br/>
 * <br/>
 * Client  --PUBLISH QoS 2-->  SERVER<br/>
 * <br/>
 * Client  <-----PUBREC------  SERVER<br/>
 * <br/>
 * Client  ------PUBREL----->  SERVER<br/>
 * <br/>
 * Client  <-----PUBCOMP-----  SERVER<br/>
 * <br/>
 */
public class MqttPublishReleaseMessageHandler implements Consumer<MqttPubRelMessage> {

  private static final Logger LOGGER = LoggerFactory.getLogger(MqttPublishReleaseMessageHandler.class);

  private final MqttEndpoint mqttEndpoint;
  private final SessionService sessionService;
  private final MsgService msgService;
  private final CompositeService compositeService;
  private final FlowService flowService;

  public MqttPublishReleaseMessageHandler(MqttEndpoint mqttEndpoint, SessionService sessionService,
                                          MsgService msgService, CompositeService compositeService,
                                          FlowService flowService) {
    this.mqttEndpoint = mqttEndpoint;
    this.sessionService = sessionService;
    this.msgService = msgService;
    this.compositeService = compositeService;
    this.flowService = flowService;
  }

  @Override
  public void accept(MqttPubRelMessage mqttPubRelMessage) {
    String clientId = mqttEndpoint.clientIdentifier();
    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("PUBREL from {}: {}", clientId, pubRelInfo(mqttPubRelMessage));
    }

    MqttProperties pubCompProperties = new MqttProperties();
    sessionService.getSessionByFields(mqttEndpoint.clientIdentifier(), new Session.Field[]{Session.Field.sessionId})
      .onItem().transformToUni(sessionFields -> msgService.getAndRemoveInboundQos2Pub((String) sessionFields.get(Session.Field.sessionId), mqttPubRelMessage.messageId()))
      .onItem().transformToUni(inboundQos2Pub -> {
        if (mqttEndpoint.protocolVersion() <= MqttVersion.MQTT_3_1_1.protocolLevel()) {
          if (inboundQos2Pub == null) {
            LOGGER.warn("PUBREL from {} without having related PUBLISH packet", clientId);
          }
          mqttEndpoint.publishComplete(mqttPubRelMessage.messageId());
        } else {
          if (inboundQos2Pub == null) {
            LOGGER.warn("PUBREL from {} without having related PUBLISH packet", clientId);
            mqttEndpoint.publishComplete(mqttPubRelMessage.messageId(), MqttPubCompReasonCode.PACKET_IDENTIFIER_NOT_FOUND, pubCompProperties);
          } else {
            mqttEndpoint.publishComplete(mqttPubRelMessage.messageId(), MqttPubCompReasonCode.SUCCESS, pubCompProperties);
          }
        }
        // From https://docs.oasis-open.org/mqtt/mqtt/v5.0/os/mqtt-v5.0-os.html#_Toc3901120: The Client MUST NOT send more than Receive Maximum QoS 1 and QoS 2 PUBLISH packets for which it has not received PUBACK, PUBCOMP, or PUBREC with a Reason Code of 128 or greater from the Server [MQTT-3.3.4-7]. If it receives more than Receive Maximum QoS 1 and QoS 2 PUBLISH packets where it has not sent a PUBACK or PUBCOMP in response, the Server uses a DISCONNECT packet with Reason Code 0x93 (Receive Maximum exceeded) as described in section 4.13 Handling errors.
        // So, MQTT broker should decrement inbound receive number when sent PUBCOMP
        flowService.decrementAndGetInboundReceive(clientId);
        return Uni.createFrom().item(inboundQos2Pub);
      })
      .onItem().transformToUni(inboundQos2Pub -> {
        if (inboundQos2Pub != null) {
          MsgToTopic msgToTopic = new MsgToTopic().setClientId(inboundQos2Pub.getClientId()).setTopic(inboundQos2Pub.getTopic())
            .setQos(inboundQos2Pub.getQos()).setPayload(inboundQos2Pub.getPayload()).setRetain(inboundQos2Pub.isRetain());
          if (mqttEndpoint.protocolVersion() > MqttVersion.MQTT_3_1_1.protocolLevel()) {
            msgToTopic.setMessageExpiryInterval(inboundQos2Pub.getMessageExpiryInterval())
              .setPayloadFormatIndicator(inboundQos2Pub.getPayloadFormatIndicator())
              .setContentType(inboundQos2Pub.getContentType())
              .setResponseTopic(inboundQos2Pub.getResponseTopic())
              .setCorrelationData(inboundQos2Pub.getCorrelationData())
              .setTopicAlias(inboundQos2Pub.getTopicAlias())
              .setUserProperties(inboundQos2Pub.getUserProperties());
          }
          return compositeService.forward(msgToTopic);
        } else {
          return Uni.createFrom().voidItem();
        }
      })
      .subscribe().with(ConsumerUtil.nothingToDo(), t -> LOGGER.error("Error occurred when handling MqttPubRelMessage", t));
  }

  private String pubRelInfo(MqttPubRelMessage mqttPubRelMessage) {
    JsonObject jsonObject = new JsonObject();
    jsonObject.put("messageId", mqttPubRelMessage.messageId());
    jsonObject.put("code", mqttPubRelMessage.code());
    jsonObject.put("properties", MqttPropertiesUtil.encode(mqttPubRelMessage.properties()));
    return jsonObject.toString();
  }

}
