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

package io.github.jpforevers.vxmq.service.client;

import io.github.jpforevers.vxmq.assist.ConsumerUtil;
import io.github.jpforevers.vxmq.assist.MqttPropertiesUtil;
import io.github.jpforevers.vxmq.service.alias.OutboundTopicAliasService;
import io.github.jpforevers.vxmq.service.flow.FlowControlService;
import io.github.jpforevers.vxmq.service.msg.MsgService;
import io.github.jpforevers.vxmq.service.msg.MsgToClient;
import io.github.jpforevers.vxmq.service.msg.OutboundQos1Pub;
import io.github.jpforevers.vxmq.service.msg.OutboundQos2Pub;
import io.github.jpforevers.vxmq.service.session.Session;
import io.github.jpforevers.vxmq.service.session.SessionService;
import io.micrometer.core.instrument.Counter;
import io.netty.handler.codec.mqtt.MqttProperties;
import io.netty.handler.codec.mqtt.MqttQoS;
import io.netty.handler.codec.mqtt.MqttVersion;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.vertx.core.AbstractVerticle;
import io.vertx.mutiny.core.buffer.Buffer;
import io.vertx.mutiny.core.eventbus.Message;
import io.vertx.mutiny.mqtt.MqttEndpoint;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;

public class ClientVerticle extends AbstractVerticle {

  private final static Logger LOGGER = LoggerFactory.getLogger(ClientVerticle.class);

  private static final int MAX_MESSAGE_ID = 65535;

  private final MqttEndpoint mqttEndpoint;
  private final SessionService sessionService;
  private final MsgService msgService;
  private final OutboundTopicAliasService outboundTopicAliasService;
  private int messageIdCounter;
  private final Counter packetsPublishSentCounter;
  private final int outboundReceiveMaximum;
  private final FlowControlService flowControlService;

  public ClientVerticle(MqttEndpoint mqttEndpoint, SessionService sessionService, MsgService msgService,
                        OutboundTopicAliasService outboundTopicAliasService, Counter packetsPublishSentCounter,
                        int outboundReceiveMaximum, FlowControlService flowControlService) {
    this.mqttEndpoint = mqttEndpoint;
    this.sessionService = sessionService;
    this.msgService = msgService;
    this.outboundTopicAliasService = outboundTopicAliasService;
    this.packetsPublishSentCounter = packetsPublishSentCounter;
    if (mqttEndpoint.protocolVersion() > MqttVersion.MQTT_3_1_1.protocolLevel()) {
      Integer outboundReceiveMaximumFromClient = MqttPropertiesUtil.getValue(mqttEndpoint.connectProperties(), MqttProperties.MqttPropertyType.RECEIVE_MAXIMUM, MqttProperties.IntegerProperty.class);
      if (outboundReceiveMaximumFromClient != null && outboundReceiveMaximumFromClient <= 65535 && outboundReceiveMaximumFromClient > 0) {
        this.outboundReceiveMaximum = outboundReceiveMaximumFromClient;
      } else {
        this.outboundReceiveMaximum = outboundReceiveMaximum;
      }
    } else {
      this.outboundReceiveMaximum = outboundReceiveMaximum;
    }
    this.flowControlService = flowControlService;
  }

  @Override
  public Uni<Void> asyncStart() {
    return vertx.eventBus().consumer(deploymentID(), this::ebMessageHandler).completionHandler();
  }

  @Override
  public Uni<Void> asyncStop() {
    return Uni.createFrom().voidItem();
  }

  private void ebMessageHandler(Message<ToClientVerticleMsg> actionMessage) {
    ToClientVerticleMsg toClientVerticleMsg = actionMessage.body();
    switch (toClientVerticleMsg.getType()) {
      case UNDEPLOY_CLIENT_VERTICLE -> handleUndeployClientVerticleAction((UndeployClientVerticleRequest) toClientVerticleMsg.getPayload());
      case CLOSE_MQTT_ENDPOINT -> handleCloseMqttEndpointAction((CloseMqttEndpointRequest) toClientVerticleMsg.getPayload());
      case DISCONNECT -> handleDisconnectAction((DisconnectRequest) toClientVerticleMsg.getPayload());
      case SEND_PUBLISH -> handleSendPublish((MsgToClient) toClientVerticleMsg.getPayload());
    }
  }

  private void handleUndeployClientVerticleAction(UndeployClientVerticleRequest undeployClientVerticleRequest) {
    vertx.undeploy(deploymentID()).subscribe().with(ConsumerUtil.nothingToDo(), t -> LOGGER.error("Error occurred when handle undeploy client verticle action", t));
  }

  private void handleCloseMqttEndpointAction(CloseMqttEndpointRequest closeMqttEndpointRequest) {
    if (mqttEndpoint.isConnected()){
      mqttEndpoint.close();
    }else {
      LOGGER.warn("Illegal state, MqttEndpoint already disconnected when try to close it");
    }
  }

  private void handleDisconnectAction(DisconnectRequest disconnectRequest) {
    mqttEndpoint.disconnect(disconnectRequest.getMqttDisconnectReasonCode(), disconnectRequest.getMqttProperties());
  }

  private void handleSendPublish(MsgToClient msgToClient) {
    // From https://docs.oasis-open.org/mqtt/mqtt/v5.0/os/mqtt-v5.0-os.html#_Toc3901120: The Server MUST NOT send more than Receive Maximum QoS 1 and QoS 2 PUBLISH packets for which it has not received PUBACK, PUBCOMP, or PUBREC with a Reason Code of 128 or greater from the Client [MQTT-3.3.4-9]. If it receives more than Receive Maximum QoS 1 and QoS 2 PUBLISH packets where it has not sent a PUBACK or PUBCOMP in response, the Client uses DISCONNECT with Reason Code 0x93 (Receive Maximum exceeded) as described in section 4.13 Handling errors.
    // So, The MQTT broker should check and increment the outbound reception number before sending a PUBLISH message.
    if (msgToClient.getQos() > 0 && flowControlService.getAndIncrementOutboundReceive(mqttEndpoint.clientIdentifier()) >= outboundReceiveMaximum) {
      LOGGER.warn("MsgToClient dropped because of the outbound reception maximum {} exceeded, MsgToClient: {}", outboundReceiveMaximum, msgToClient);
      return;
    }

    int messageId;
    if (msgToClient.getMessageId() == null || msgToClient.getMessageId() <= 0 || msgToClient.getMessageId() >= MAX_MESSAGE_ID) {
      this.messageIdCounter = ((messageIdCounter % MAX_MESSAGE_ID) != 0) ? messageIdCounter + 1 : 1;
      messageId = this.messageIdCounter;
    } else {
      messageId = msgToClient.getMessageId();
    }
    Uni.createFrom().voidItem()
      .onItem().transformToUni(v -> {
        Integer topicAliasMax = MqttPropertiesUtil.getValue(mqttEndpoint.connectProperties(), MqttProperties.MqttPropertyType.TOPIC_ALIAS_MAXIMUM, MqttProperties.IntegerProperty.class);
        return outboundTopicAliasService.processTopicAlias(msgToClient, mqttEndpoint.clientIdentifier(), topicAliasMax);
      })
      .onItem().transformToUni(v -> sessionService.getSessionByFields(mqttEndpoint.clientIdentifier(), new Session.Field[]{Session.Field.sessionId, Session.Field.clientId}))
      .onItem().transformToUni(sessionFields -> {
        MqttQoS mqttQoS = MqttQoS.valueOf(msgToClient.getQos());
        return switch (mqttQoS) {
          case AT_LEAST_ONCE ->
            msgService.saveOutboundQos1Pub(new OutboundQos1Pub((String) sessionFields.get(Session.Field.sessionId), (String) sessionFields.get(Session.Field.clientId),
              messageId, msgToClient.getTopic(), msgToClient.getQos(), msgToClient.getPayload(),
              msgToClient.isDup(), msgToClient.isRetain(),
              msgToClient.getMessageExpiryInterval(), msgToClient.getPayloadFormatIndicator(),
              msgToClient.getContentType(), msgToClient.getResponseTopic(), msgToClient.getCorrelationData(),
              msgToClient.getSubscriptionIdentifier(), msgToClient.getTopicAlias(), msgToClient.getUserProperties(),
              Instant.now().toEpochMilli()));
          case EXACTLY_ONCE ->
            msgService.saveOutboundQos2Pub(new OutboundQos2Pub((String) sessionFields.get(Session.Field.sessionId), (String) sessionFields.get(Session.Field.clientId),
              messageId, msgToClient.getTopic(), msgToClient.getQos(), msgToClient.getPayload(),
              msgToClient.isDup(), msgToClient.isRetain(),
              msgToClient.getMessageExpiryInterval(), msgToClient.getPayloadFormatIndicator(),
              msgToClient.getContentType(), msgToClient.getResponseTopic(), msgToClient.getCorrelationData(),
              msgToClient.getSubscriptionIdentifier(), msgToClient.getTopicAlias(), msgToClient.getUserProperties(),
              Instant.now().toEpochMilli()));
          default -> Uni.createFrom().voidItem();
        };
      })
      // From MQTT 3.1.1 specification: The DUP flag MUST be set to 0 for all QoS 0 messages
      .onItem().transformToUni(v -> {
        if (mqttEndpoint.protocolVersion() <= MqttVersion.MQTT_3_1_1.protocolLevel()) {
          return mqttEndpoint.publish(msgToClient.getTopic(), Buffer.newInstance(msgToClient.getPayload()), MqttQoS.valueOf(msgToClient.getQos()), msgToClient.getQos() != MqttQoS.AT_MOST_ONCE.value() && msgToClient.isDup(), msgToClient.isRetain(), messageId);
        } else {
          MqttProperties mqttProperties = new MqttProperties();
          if (msgToClient.getMessageExpiryInterval() != null && msgToClient.getMessageExpiryInterval() != 0) {
            long messageExpiryInterval = msgToClient.getMessageExpiryInterval() - (Instant.now().toEpochMilli() - msgToClient.getCreatedTime()) / 1000;
            mqttProperties.add(new MqttProperties.IntegerProperty(MqttProperties.MqttPropertyType.PUBLICATION_EXPIRY_INTERVAL.value(), (int) messageExpiryInterval));
          }
          if (msgToClient.getPayloadFormatIndicator() != null) {
            mqttProperties.add(new MqttProperties.IntegerProperty(MqttProperties.MqttPropertyType.PAYLOAD_FORMAT_INDICATOR.value(), msgToClient.getPayloadFormatIndicator()));
          }
          if (StringUtils.isNotBlank(msgToClient.getContentType())) {
            mqttProperties.add(new MqttProperties.StringProperty(MqttProperties.MqttPropertyType.CONTENT_TYPE.value(), msgToClient.getContentType()));
          }
          if (StringUtils.isNotBlank(msgToClient.getResponseTopic())) {
            mqttProperties.add(new MqttProperties.StringProperty(MqttProperties.MqttPropertyType.RESPONSE_TOPIC.value(), msgToClient.getResponseTopic()));
          }
          if (msgToClient.getCorrelationData() != null) {
            mqttProperties.add(new MqttProperties.BinaryProperty(MqttProperties.MqttPropertyType.CORRELATION_DATA.value(), msgToClient.getCorrelationData().getBytes()));
          }
          if (msgToClient.getSubscriptionIdentifier() != null) {
            mqttProperties.add(new MqttProperties.IntegerProperty(MqttProperties.MqttPropertyType.SUBSCRIPTION_IDENTIFIER.value(), msgToClient.getSubscriptionIdentifier()));
          }
          if (msgToClient.getTopicAlias() != null) {
            mqttProperties.add(new MqttProperties.IntegerProperty(MqttProperties.MqttPropertyType.TOPIC_ALIAS.value(), msgToClient.getTopicAlias()));
          }
          return mqttEndpoint.publish(msgToClient.getTopic(), Buffer.newInstance(msgToClient.getPayload()), MqttQoS.valueOf(msgToClient.getQos()), msgToClient.getQos() != MqttQoS.AT_MOST_ONCE.value() && msgToClient.isDup(), msgToClient.isRetain(), messageId, mqttProperties);
        }
      })
      .onItem().invoke(() -> {
        if (packetsPublishSentCounter != null) {
          packetsPublishSentCounter.increment();
        }
      })
      .subscribe().with(ConsumerUtil.nothingToDo(), t -> LOGGER.error("Error occurred when sending PUBLISH to client {}", mqttEndpoint.clientIdentifier(), t));
  }

  public String getClientId() {
    return mqttEndpoint.clientIdentifier();
  }

}
