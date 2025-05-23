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
import io.github.jpforevers.vxmq.assist.TopicUtil;
import io.github.jpforevers.vxmq.assist.VertxUtil;
import io.github.jpforevers.vxmq.event.Event;
import io.github.jpforevers.vxmq.event.EventService;
import io.github.jpforevers.vxmq.event.mqtt.MqttPublishInboundAcceptedEvent;
import io.github.jpforevers.vxmq.mqtt.exception.MqttPublishException;
import io.github.jpforevers.vxmq.service.composite.CompositeService;
import io.github.jpforevers.vxmq.service.flow.FlowControlService;
import io.github.jpforevers.vxmq.service.msg.InboundQos2Pub;
import io.github.jpforevers.vxmq.service.msg.MsgService;
import io.github.jpforevers.vxmq.service.msg.MsgToTopic;
import io.github.jpforevers.vxmq.service.retain.Retain;
import io.github.jpforevers.vxmq.service.retain.RetainService;
import io.github.jpforevers.vxmq.service.session.Session;
import io.github.jpforevers.vxmq.service.session.SessionService;
import io.micrometer.core.instrument.Counter;
import io.netty.handler.codec.mqtt.MqttProperties;
import io.netty.handler.codec.mqtt.MqttVersion;
import io.smallrye.mutiny.Uni;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;
import io.vertx.mqtt.messages.codes.MqttDisconnectReasonCode;
import io.vertx.mqtt.messages.codes.MqttPubAckReasonCode;
import io.vertx.mqtt.messages.codes.MqttPubRecReasonCode;
import io.vertx.mutiny.core.Vertx;
import io.vertx.mutiny.mqtt.MqttEndpoint;
import io.vertx.mutiny.mqtt.messages.MqttPublishMessage;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.List;
import java.util.function.Consumer;

public class MqttPublishHandler implements Consumer<MqttPublishMessage> {

  private static final Logger LOGGER = LoggerFactory.getLogger(MqttPublishHandler.class);

  private final MqttEndpoint mqttEndpoint;
  private final Vertx vertx;
  private final MsgService msgService;
  private final SessionService sessionService;
  private final RetainService retainService;
  private final CompositeService compositeService;
  private final EventService eventService;
  private final Counter packetsPublishReceivedCounter;
  private final int inboundReceiveMaximum;
  private final FlowControlService flowControlService;

  public MqttPublishHandler(MqttEndpoint mqttEndpoint, Vertx vertx,
                            MsgService msgService, SessionService sessionService,
                            RetainService retainService, CompositeService compositeService, EventService eventService,
                            Counter packetsPublishReceivedCounter, int inboundReceiveMaximum, FlowControlService flowControlService) {
    this.mqttEndpoint = mqttEndpoint;
    this.vertx = vertx;
    this.msgService = msgService;
    this.sessionService = sessionService;
    this.retainService = retainService;
    this.compositeService = compositeService;
    this.eventService = eventService;
    this.packetsPublishReceivedCounter = packetsPublishReceivedCounter;
    this.inboundReceiveMaximum = inboundReceiveMaximum;
    this.flowControlService = flowControlService;
  }

  @Override
  public void accept(MqttPublishMessage mqttPublishMessage) {
    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("PUBLISH from {}: {}", mqttEndpoint.clientIdentifier(), publicationInfo(mqttPublishMessage));
    }

    if (packetsPublishReceivedCounter != null) {
      packetsPublishReceivedCounter.increment();
    }

    // From https://docs.oasis-open.org/mqtt/mqtt/v5.0/os/mqtt-v5.0-os.html#_Toc3901120: The Client MUST NOT send more than Receive Maximum QoS 1 and QoS 2 PUBLISH packets for which it has not received PUBACK, PUBCOMP, or PUBREC with a Reason Code of 128 or greater from the Server [MQTT-3.3.4-7]. If it receives more than Receive Maximum QoS 1 and QoS 2 PUBLISH packets where it has not sent a PUBACK or PUBCOMP in response, the Server uses a DISCONNECT packet with Reason Code 0x93 (Receive Maximum exceeded) as described in section 4.13 Handling errors.
    // So, The MQTT broker should increment and check the inbound reception number when a PUBLISH received
    if (mqttPublishMessage.qosLevel().value() > 0 && flowControlService.incrementAndGetInboundReceive(mqttEndpoint.clientIdentifier()) > inboundReceiveMaximum) {
      if (mqttEndpoint.protocolVersion() > MqttVersion.MQTT_3_1_1.protocolLevel()) {
        mqttEndpoint.disconnect(MqttDisconnectReasonCode.RECEIVE_MAXIMUM_EXCEEDED, MqttProperties.NO_PROPERTIES);
      } else {
        mqttEndpoint.close();
      }
      return;
    }

    sessionService.updateLatestUpdatedTime(mqttEndpoint.clientIdentifier(), Instant.now().toEpochMilli())
      .subscribe().with(ConsumerUtil.nothingToDo(), t -> LOGGER.error("Error occurred when updating session latest updatedTime", t));

    MqttProperties pubAckProperties = new MqttProperties();
    MqttProperties pubRecProperties = new MqttProperties();
    Uni.createFrom().voidItem()
      .onItem().transformToUni(v -> checkTopic(mqttPublishMessage))
      .onItem().transformToUni(v -> authorize(mqttPublishMessage))
      .onItem().transformToUni(v -> sessionService.getSessionByFields(mqttEndpoint.clientIdentifier(), new Session.Field[]{Session.Field.sessionId}))
      .onItem().call(sessionFields -> handleQos(mqttPublishMessage, (String) sessionFields.get(Session.Field.sessionId)))
      .onItem().call(sessionFields -> publishEvent((String) sessionFields.get(Session.Field.sessionId), mqttPublishMessage))
      .subscribe().with(v -> {
        if (LOGGER.isDebugEnabled()) {
          LOGGER.debug("PUBLISH from {} to {} accepted", mqttEndpoint.clientIdentifier(), mqttPublishMessage.topicName());
        }
        if (mqttEndpoint.protocolVersion() <= MqttVersion.MQTT_3_1_1.protocolLevel()) {
          switch (mqttPublishMessage.qosLevel()) {
            case AT_MOST_ONCE -> {
            }
            case AT_LEAST_ONCE -> {
              mqttEndpoint.publishAcknowledge(mqttPublishMessage.messageId());
              // From https://docs.oasis-open.org/mqtt/mqtt/v5.0/os/mqtt-v5.0-os.html#_Toc3901120: The Client MUST NOT send more than Receive Maximum QoS 1 and QoS 2 PUBLISH packets for which it has not received PUBACK, PUBCOMP, or PUBREC with a Reason Code of 128 or greater from the Server [MQTT-3.3.4-7]. If it receives more than Receive Maximum QoS 1 and QoS 2 PUBLISH packets where it has not sent a PUBACK or PUBCOMP in response, the Server uses a DISCONNECT packet with Reason Code 0x93 (Receive Maximum exceeded) as described in section 4.13 Handling errors.
              // So, The MQTT broker should decrement the inbound reception number when sent a PUBACK
              flowControlService.decrementInboundReceive(mqttEndpoint.clientIdentifier());
            }
            case EXACTLY_ONCE -> mqttEndpoint.publishReceived(mqttPublishMessage.messageId());
          }
        } else {
          switch (mqttPublishMessage.qosLevel()) {
            case AT_MOST_ONCE -> {
            }
            case AT_LEAST_ONCE -> {
              mqttEndpoint.publishAcknowledge(mqttPublishMessage.messageId(), MqttPubAckReasonCode.SUCCESS, pubAckProperties);
              // From https://docs.oasis-open.org/mqtt/mqtt/v5.0/os/mqtt-v5.0-os.html#_Toc3901120: The Client MUST NOT send more than Receive Maximum QoS 1 and QoS 2 PUBLISH packets for which it has not received PUBACK, PUBCOMP, or PUBREC with a Reason Code of 128 or greater from the Server [MQTT-3.3.4-7]. If it receives more than Receive Maximum QoS 1 and QoS 2 PUBLISH packets where it has not sent a PUBACK or PUBCOMP in response, the Server uses a DISCONNECT packet with Reason Code 0x93 (Receive Maximum exceeded) as described in section 4.13 Handling errors.
              // So, The MQTT broker should decrement the inbound reception number when sent a PUBACK
              flowControlService.decrementInboundReceive(mqttEndpoint.clientIdentifier());
            }
            case EXACTLY_ONCE ->
              mqttEndpoint.publishReceived(mqttPublishMessage.messageId(), MqttPubRecReasonCode.SUCCESS, pubRecProperties);
          }
        }
        handleRetain(mqttPublishMessage).subscribe().with(ConsumerUtil.nothingToDo(), t -> LOGGER.error("Error occurred when processing retain of PUBLISH of {}", mqttEndpoint.clientIdentifier(), t));
        handleMessage(mqttPublishMessage).subscribe().with(ConsumerUtil.nothingToDo(), t -> LOGGER.error("Error occurred when distributing PUBLISH of {}", mqttEndpoint.clientIdentifier(), t));
      }, t -> {
        LOGGER.error("Error occurred when processing PUBLISH from {} to {}", mqttEndpoint.clientIdentifier(), mqttPublishMessage.topicName(), t);
        if (mqttEndpoint.protocolVersion() <= MqttVersion.MQTT_3_1_1.protocolLevel()) {
          mqttEndpoint.close();
        } else {
          Integer requestProblemInformation = MqttPropertiesUtil.getValue(mqttEndpoint.connectProperties(), MqttProperties.MqttPropertyType.REQUEST_PROBLEM_INFORMATION, MqttProperties.IntegerProperty.class);
          if (t instanceof MqttPublishException) {
            switch (mqttPublishMessage.qosLevel()) {
              case AT_MOST_ONCE:
                break;
              case AT_LEAST_ONCE:
                // If the value of Request Problem Information is 0, the Server MAY return a Reason String or User Properties on a CONNACK or DISCONNECT packet, but MUST NOT send a Reason String or User Properties on any packet other than PUBLISH, CONNACK, or DISCONNECT [MQTT-3.1.2-29]
                if (requestProblemInformation == null || requestProblemInformation == 1) {
                  pubAckProperties.add(new MqttProperties.StringProperty(MqttProperties.MqttPropertyType.REASON_STRING.value(), t.getMessage()));
                }
                mqttEndpoint.publishAcknowledge(mqttPublishMessage.messageId(), ((MqttPublishException) t).getMqttPubAckReasonCode(), pubAckProperties);
                break;
              case EXACTLY_ONCE:
                // If the value of Request Problem Information is 0, the Server MAY return a Reason String or User Properties on a CONNACK or DISCONNECT packet, but MUST NOT send a Reason String or User Properties on any packet other than PUBLISH, CONNACK, or DISCONNECT [MQTT-3.1.2-29]
                if (requestProblemInformation == null || requestProblemInformation == 1) {
                  pubRecProperties.add(new MqttProperties.StringProperty(MqttProperties.MqttPropertyType.REASON_STRING.value(), t.getMessage()));
                }
                mqttEndpoint.publishReceived(mqttPublishMessage.messageId(), ((MqttPublishException) t).getMqttPubRecReasonCode(), pubRecProperties);
                // From https://docs.oasis-open.org/mqtt/mqtt/v5.0/os/mqtt-v5.0-os.html#_Toc3901120: The Client MUST NOT send more than Receive Maximum QoS 1 and QoS 2 PUBLISH packets for which it has not received PUBACK, PUBCOMP, or PUBREC with a Reason Code of 128 or greater from the Server [MQTT-3.3.4-7]. If it receives more than Receive Maximum QoS 1 and QoS 2 PUBLISH packets where it has not sent a PUBACK or PUBCOMP in response, the Server uses a DISCONNECT packet with Reason Code 0x93 (Receive Maximum exceeded) as described in section 4.13 Handling errors.
                // So, The MQTT broker should decrement the inbound reception number before sent a PUBREC message with a Reason Code of 128 or greater.
                if (((MqttPublishException) t).getMqttPubRecReasonCode().isError()) {
                  flowControlService.decrementInboundReceive(mqttEndpoint.clientIdentifier());
                }
            }
          } else {
            switch (mqttPublishMessage.qosLevel()) {
              case AT_MOST_ONCE:
                break;
              case AT_LEAST_ONCE: {
                // If the value of Request Problem Information is 0, the Server MAY return a Reason String or User Properties on a CONNACK or DISCONNECT packet, but MUST NOT send a Reason String or User Properties on any packet other than PUBLISH, CONNACK, or DISCONNECT [MQTT-3.1.2-29]
                if (requestProblemInformation == null || requestProblemInformation == 1) {
                  pubAckProperties.add(new MqttProperties.StringProperty(MqttProperties.MqttPropertyType.REASON_STRING.value(), t.getMessage()));
                }
                mqttEndpoint.publishAcknowledge(mqttPublishMessage.messageId(), MqttPubAckReasonCode.UNSPECIFIED_ERROR, pubAckProperties);
                break;
              }
              case EXACTLY_ONCE:
                // If the value of Request Problem Information is 0, the Server MAY return a Reason String or User Properties on a CONNACK or DISCONNECT packet, but MUST NOT send a Reason String or User Properties on any packet other than PUBLISH, CONNACK, or DISCONNECT [MQTT-3.1.2-29]
                if (requestProblemInformation == null || requestProblemInformation == 1) {
                  pubRecProperties.add(new MqttProperties.StringProperty(MqttProperties.MqttPropertyType.REASON_STRING.value(), t.getMessage()));
                }
                mqttEndpoint.publishReceived(mqttPublishMessage.messageId(), MqttPubRecReasonCode.UNSPECIFIED_ERROR, pubRecProperties);
                // From https://docs.oasis-open.org/mqtt/mqtt/v5.0/os/mqtt-v5.0-os.html#_Toc3901120: The Client MUST NOT send more than Receive Maximum QoS 1 and QoS 2 PUBLISH packets for which it has not received PUBACK, PUBCOMP, or PUBREC with a Reason Code of 128 or greater from the Server [MQTT-3.3.4-7]. If it receives more than Receive Maximum QoS 1 and QoS 2 PUBLISH packets where it has not sent a PUBACK or PUBCOMP in response, the Server uses a DISCONNECT packet with Reason Code 0x93 (Receive Maximum exceeded) as described in section 4.13 Handling errors.
                // So, The MQTT broker should decrement the inbound reception number before sent a PUBREC message with a Reason Code of 128 or greater.
                if (((MqttPublishException) t).getMqttPubRecReasonCode().isError()) {
                  flowControlService.decrementInboundReceive(mqttEndpoint.clientIdentifier());
                }
            }
          }
        }
      });
  }

  private String publicationInfo(MqttPublishMessage mqttPublishMessage) {
    JsonObject jsonObject = new JsonObject();
    jsonObject.put("topicName", mqttPublishMessage.topicName());
    jsonObject.put("mqttQoS", mqttPublishMessage.qosLevel());
    jsonObject.put("messageId", mqttPublishMessage.messageId());
    jsonObject.put("payload", mqttPublishMessage.payload().getDelegate());
    jsonObject.put("dup", mqttPublishMessage.isDup());
    jsonObject.put("retain", mqttPublishMessage.isRetain());
    jsonObject.put("properties", MqttPropertiesUtil.encode(mqttPublishMessage.properties()));
    return jsonObject.toString();
  }

  /**
   * Check topic
   *
   * @param mqttPublishMessage mqttPublishMessage
   * @return Void
   */
  private Uni<Void> checkTopic(MqttPublishMessage mqttPublishMessage) {
    if (StringUtils.isNotBlank(mqttPublishMessage.topicName())) {
      if (TopicUtil.isValidTopicToPublish(mqttPublishMessage.topicName())) {
        return Uni.createFrom().voidItem();
      } else {
        return switch (mqttPublishMessage.qosLevel()) {
          case AT_MOST_ONCE -> Uni.createFrom().failure(new MqttPublishException("Topic name invalid"));
          case AT_LEAST_ONCE ->
            Uni.createFrom().failure(new MqttPublishException(MqttPubAckReasonCode.TOPIC_NAME_INVALID));
          case EXACTLY_ONCE ->
            Uni.createFrom().failure(new MqttPublishException(MqttPubRecReasonCode.TOPIC_NAME_INVALID));
          default -> Uni.createFrom().failure(new MqttPublishException("Unknown mqtt qos"));
        };
      }
    } else {
      // For MQTT 5, topic name may be empty
      return Uni.createFrom().voidItem();
    }
  }

  /**
   * Authorize
   *
   * @param mqttPublishMessage mqttPublishMessage
   * @return Void
   */
  private Uni<Void> authorize(MqttPublishMessage mqttPublishMessage) {
    // TODO 认证机制
    boolean authorizationResult = true;
    if (authorizationResult) {
      return Uni.createFrom().voidItem();
    } else {
      if (mqttEndpoint.protocolVersion() <= MqttVersion.MQTT_3_1_1.protocolLevel()) {
        return Uni.createFrom().failure(new MqttPublishException("Not authorized"));
      } else {
        return Uni.createFrom().failure(new MqttPublishException(MqttPubAckReasonCode.NOT_AUTHORIZED));
      }
    }
  }

  /**
   * Handle QoS.
   *
   * @param mqttPublishMessage mqttPublishMessage
   * @param sessionId          sessionId
   * @return Void
   */
  private Uni<Void> handleQos(MqttPublishMessage mqttPublishMessage, String sessionId) {
    return switch (mqttPublishMessage.qosLevel()) {
      case AT_MOST_ONCE, AT_LEAST_ONCE ->
        // Nothing need to do.
        Uni.createFrom().voidItem();
      case EXACTLY_ONCE -> Uni.createFrom().voidItem()
        .onItem().transformToUni(v -> {
          Integer messageExpiryInterval = null;
          Integer payloadFormatIndicator = null;
          String responseTopic = null;
          Buffer correlationData = null;
          String contentType = null;
          Integer topicAlias = null;
          List<MqttProperties.StringPair> userProperties = List.of();
          if (mqttEndpoint.protocolVersion() > MqttVersion.MQTT_3_1_1.protocolLevel()) {
            messageExpiryInterval = MqttPropertiesUtil.getValue(mqttPublishMessage.properties(), MqttProperties.MqttPropertyType.PUBLICATION_EXPIRY_INTERVAL, MqttProperties.IntegerProperty.class);
            payloadFormatIndicator = MqttPropertiesUtil.getValue(mqttPublishMessage.properties(), MqttProperties.MqttPropertyType.PAYLOAD_FORMAT_INDICATOR, MqttProperties.IntegerProperty.class);
            contentType = MqttPropertiesUtil.getValue(mqttPublishMessage.properties(), MqttProperties.MqttPropertyType.CONTENT_TYPE, MqttProperties.StringProperty.class);
            responseTopic = MqttPropertiesUtil.getValue(mqttPublishMessage.properties(), MqttProperties.MqttPropertyType.RESPONSE_TOPIC, MqttProperties.StringProperty.class);
            byte[] correlationDataBytes = MqttPropertiesUtil.getValue(mqttEndpoint.connectProperties(), MqttProperties.MqttPropertyType.CORRELATION_DATA, MqttProperties.BinaryProperty.class);
            correlationData = correlationDataBytes != null ? Buffer.buffer(correlationDataBytes) : null;
            topicAlias = MqttPropertiesUtil.getValue(mqttPublishMessage.properties(), MqttProperties.MqttPropertyType.TOPIC_ALIAS, MqttProperties.IntegerProperty.class);
            userProperties = MqttPropertiesUtil.getValues(mqttPublishMessage.properties(), MqttProperties.MqttPropertyType.USER_PROPERTY, MqttProperties.UserProperty.class);
          }
          InboundQos2Pub inboundQos2Pub = new InboundQos2Pub(sessionId, mqttEndpoint.clientIdentifier(),
            mqttPublishMessage.messageId(), mqttPublishMessage.topicName(), mqttPublishMessage.qosLevel().value(),
            mqttPublishMessage.payload().getDelegate(), mqttPublishMessage.isDup(), mqttPublishMessage.isRetain(),
            messageExpiryInterval, payloadFormatIndicator, contentType,
            responseTopic, correlationData, topicAlias,
            userProperties, Instant.now().toEpochMilli());
          return msgService.saveInboundQos2Pub(inboundQos2Pub);
        });
      default -> Uni.createFrom().failure(new MqttPublishException("Unknown mqtt qos"));
    };
  }

  private Uni<Void> publishEvent(String sessionId, MqttPublishMessage mqttPublishMessage) {
    Event event = new MqttPublishInboundAcceptedEvent(Instant.now().toEpochMilli(), VertxUtil.getNodeId(vertx),
      mqttEndpoint.clientIdentifier(), sessionId, mqttPublishMessage.topicName(), mqttPublishMessage.qosLevel().value(),
      mqttPublishMessage.messageId(), mqttPublishMessage.payload().getDelegate(), mqttPublishMessage.isDup(), mqttPublishMessage.isRetain());
    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("Publishing event: {}, ", event.toJson());
    }
    return eventService.publishEvent(event);
  }

  /**
   * Handle retain.
   *
   * @param mqttPublishMessage mqttPublishMessage
   * @return Void
   */
  private Uni<Void> handleRetain(MqttPublishMessage mqttPublishMessage) {
    if (mqttPublishMessage.isRetain()) {
      if (mqttPublishMessage.payload() != null && mqttPublishMessage.payload().length() > 0) {
        Integer messageExpiryInterval = null;
        Integer payloadFormatIndicator = null;
        String contentType = null;
        if (mqttEndpoint.protocolVersion() > MqttVersion.MQTT_3_1_1.protocolLevel()) {
          messageExpiryInterval = MqttPropertiesUtil.getValue(mqttPublishMessage.properties(), MqttProperties.MqttPropertyType.PUBLICATION_EXPIRY_INTERVAL, MqttProperties.IntegerProperty.class);
          payloadFormatIndicator = MqttPropertiesUtil.getValue(mqttPublishMessage.properties(), MqttProperties.MqttPropertyType.PAYLOAD_FORMAT_INDICATOR, MqttProperties.IntegerProperty.class);
          contentType = MqttPropertiesUtil.getValue(mqttPublishMessage.properties(), MqttProperties.MqttPropertyType.CONTENT_TYPE, MqttProperties.StringProperty.class);
        }
        Retain retainMessage = new Retain(mqttPublishMessage.topicName(), mqttPublishMessage.qosLevel().value(),
          mqttPublishMessage.payload().getDelegate(), messageExpiryInterval, payloadFormatIndicator, contentType, Instant.now().toEpochMilli());
        return retainService.saveOrUpdateRetain(retainMessage);
      } else {
        return retainService.removeRetain(mqttPublishMessage.topicName());
      }
    } else {
      return Uni.createFrom().voidItem();
    }
  }

  /**
   * Handle message
   *
   * @param mqttPublishMessage mqttPublishMessage
   * @return Void
   */
  private Uni<Void> handleMessage(MqttPublishMessage mqttPublishMessage) {
    switch (mqttPublishMessage.qosLevel()) {
      case AT_MOST_ONCE, AT_LEAST_ONCE -> {
        MsgToTopic msgToTopic = new MsgToTopic().setClientId(mqttEndpoint.clientIdentifier()).setTopic(mqttPublishMessage.topicName())
          .setQos(mqttPublishMessage.qosLevel().value()).setPayload(mqttPublishMessage.payload().getDelegate())
          .setRetain(mqttPublishMessage.isRetain());
        if (mqttEndpoint.protocolVersion() > MqttVersion.MQTT_3_1_1.protocolLevel()) {
          Integer messageExpiryInterval = MqttPropertiesUtil.getValue(mqttPublishMessage.properties(), MqttProperties.MqttPropertyType.PUBLICATION_EXPIRY_INTERVAL, MqttProperties.IntegerProperty.class);
          Integer payloadFormatIndicator = MqttPropertiesUtil.getValue(mqttPublishMessage.properties(), MqttProperties.MqttPropertyType.PAYLOAD_FORMAT_INDICATOR, MqttProperties.IntegerProperty.class);
          String contentType = MqttPropertiesUtil.getValue(mqttPublishMessage.properties(), MqttProperties.MqttPropertyType.CONTENT_TYPE, MqttProperties.StringProperty.class);
          String responseTopic = MqttPropertiesUtil.getValue(mqttPublishMessage.properties(), MqttProperties.MqttPropertyType.RESPONSE_TOPIC, MqttProperties.StringProperty.class);
          byte[] correlationDataBytes = MqttPropertiesUtil.getValue(mqttEndpoint.connectProperties(), MqttProperties.MqttPropertyType.CORRELATION_DATA, MqttProperties.BinaryProperty.class);
          Buffer correlationData = correlationDataBytes != null ? Buffer.buffer(correlationDataBytes) : null;
          Integer topicAlias = MqttPropertiesUtil.getValue(mqttPublishMessage.properties(), MqttProperties.MqttPropertyType.TOPIC_ALIAS, MqttProperties.IntegerProperty.class);
          List<MqttProperties.StringPair> userProperties = MqttPropertiesUtil.getValues(mqttPublishMessage.properties(), MqttProperties.MqttPropertyType.USER_PROPERTY, MqttProperties.UserProperty.class);
          msgToTopic.setMessageExpiryInterval(messageExpiryInterval);
          msgToTopic.setPayloadFormatIndicator(payloadFormatIndicator);
          msgToTopic.setContentType(contentType);
          msgToTopic.setResponseTopic(responseTopic);
          msgToTopic.setCorrelationData(correlationData);
          msgToTopic.setTopicAlias(topicAlias);
          msgToTopic.setUserProperties(userProperties);
        }
        return compositeService.forward(msgToTopic);
      }
      case EXACTLY_ONCE -> {
        return Uni.createFrom().voidItem();
      }
      default -> {
        return Uni.createFrom().failure(new MqttPublishException("Unknown mqtt qos"));
      }
    }
  }

}
