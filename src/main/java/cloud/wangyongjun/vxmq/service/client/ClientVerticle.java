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

package cloud.wangyongjun.vxmq.service.client;

import cloud.wangyongjun.vxmq.assist.ConsumerUtil;
import cloud.wangyongjun.vxmq.assist.EBHeader;
import cloud.wangyongjun.vxmq.service.msg.MsgService;
import cloud.wangyongjun.vxmq.service.msg.MsgToClient;
import cloud.wangyongjun.vxmq.service.msg.OutboundQos1Pub;
import cloud.wangyongjun.vxmq.service.msg.OutboundQos2Pub;
import cloud.wangyongjun.vxmq.service.session.SessionService;
import io.micrometer.core.instrument.Counter;
import io.netty.handler.codec.mqtt.MqttProperties;
import io.netty.handler.codec.mqtt.MqttQoS;
import io.netty.handler.codec.mqtt.MqttVersion;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.vertx.core.AbstractVerticle;
import io.vertx.core.json.JsonObject;
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
  private int messageIdCounter;
  private final Counter packetsPublishSentCounter;

  public ClientVerticle(MqttEndpoint mqttEndpoint, SessionService sessionService, MsgService msgService, Counter packetsPublishSentCounter) {
    this.mqttEndpoint = mqttEndpoint;
    this.sessionService = sessionService;
    this.msgService = msgService;
    this.packetsPublishSentCounter = packetsPublishSentCounter;
  }

  @Override
  public Uni<Void> asyncStart() {
    return vertx.eventBus().consumer(deploymentID(), this::ebMessageHandler).completionHandler();
  }

  @Override
  public Uni<Void> asyncStop() {
    return Uni.createFrom().voidItem();
  }

  private void ebMessageHandler(Message<JsonObject> actionMessage) {
    String actionString = actionMessage.headers().get(EBHeader.ACTION.name());
    if (StringUtils.isNotBlank(actionString)) {
      ClientVerticleAction actionEnum = ClientVerticleAction.valueOf(actionString);
      switch (actionEnum) {
        case UNDEPLOY_CLIENT_VERTICLE -> handleUndeployClientVerticleAction();
        case CLOSE_MQTT_ENDPOINT -> handleCloseMqttEndpointAction();
        case DISCONNECT -> handleDisconnectAction(actionMessage);
        case SEND_PUBLISH -> handleSendPublish(actionMessage);
      }
    }
  }

  private void handleUndeployClientVerticleAction() {
    vertx.undeploy(deploymentID()).subscribe().with(ConsumerUtil.nothingToDo(), t -> LOGGER.error("Error occurred when handle undeploy client verticle action", t));
  }

  private void handleCloseMqttEndpointAction() {
    if (mqttEndpoint.isConnected()){
      mqttEndpoint.close();
    }else {
      LOGGER.warn("Illegal state, MqttEndpoint already disconnected when try to close it");
    }
  }

  private void handleDisconnectAction(Message<JsonObject> actionMessage) {
    DisconnectRequest disconnectRequest = new DisconnectRequest(actionMessage.body());
    mqttEndpoint.disconnect(disconnectRequest.getMqttDisconnectReasonCode(), disconnectRequest.getMqttProperties());
  }

  private void handleSendPublish(Message<JsonObject> actionMessage) {
    MsgToClient msgToClient = new MsgToClient(actionMessage.body());
    int messageId;
    if (msgToClient.getMessageId() == null || msgToClient.getMessageId() <= 0 || msgToClient.getMessageId() >= MAX_MESSAGE_ID) {
      this.messageIdCounter = ((messageIdCounter % MAX_MESSAGE_ID) != 0) ? messageIdCounter + 1 : 1;
      messageId = this.messageIdCounter;
    } else {
      messageId = msgToClient.getMessageId();
    }
    sessionService.getSession(mqttEndpoint.clientIdentifier())
      .onItem().transformToUni(session -> {
        MqttQoS mqttQoS = MqttQoS.valueOf(msgToClient.getQos());
        return switch (mqttQoS) {
          case AT_LEAST_ONCE ->
            msgService.saveOutboundQos1Pub(new OutboundQos1Pub(session.getSessionId(), session.getClientId(),
              messageId, msgToClient.getTopic(), msgToClient.getQos(), msgToClient.getPayload(),
              msgToClient.isDup(), msgToClient.isRetain(),
              msgToClient.getMessageExpiryInterval(), msgToClient.getPayloadFormatIndicator(),
              msgToClient.getContentType(), msgToClient.getResponseTopic(), msgToClient.getCorrelationData(),
              msgToClient.getSubscriptionIdentifier(), msgToClient.getUserProperties(),
              Instant.now().toEpochMilli()));
          case EXACTLY_ONCE ->
            msgService.saveOutboundQos2Pub(new OutboundQos2Pub(session.getSessionId(), session.getClientId(),
              messageId, msgToClient.getTopic(), msgToClient.getQos(), msgToClient.getPayload(),
              msgToClient.isDup(), msgToClient.isRetain(),
              msgToClient.getMessageExpiryInterval(), msgToClient.getPayloadFormatIndicator(),
              msgToClient.getContentType(), msgToClient.getResponseTopic(), msgToClient.getCorrelationData(),
              msgToClient.getSubscriptionIdentifier(), msgToClient.getUserProperties(),
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
