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
import cloud.wangyongjun.vxmq.assist.VertxUtil;
import cloud.wangyongjun.vxmq.event.Event;
import cloud.wangyongjun.vxmq.event.EventService;
import cloud.wangyongjun.vxmq.event.mqtt.MqttPublishInboundAcceptedEvent;
import cloud.wangyongjun.vxmq.assist.MqttPropertiesUtil;
import cloud.wangyongjun.vxmq.assist.TopicUtil;
import cloud.wangyongjun.vxmq.service.composite.CompositeService;
import cloud.wangyongjun.vxmq.mqtt.exception.MqttPublishException;
import cloud.wangyongjun.vxmq.service.msg.InboundQos2Pub;
import cloud.wangyongjun.vxmq.service.msg.MsgService;
import cloud.wangyongjun.vxmq.service.msg.MsgToTopic;
import cloud.wangyongjun.vxmq.service.retain.Retain;
import cloud.wangyongjun.vxmq.service.retain.RetainService;
import cloud.wangyongjun.vxmq.service.session.Session;
import cloud.wangyongjun.vxmq.service.session.SessionService;
import io.netty.handler.codec.mqtt.MqttProperties;
import io.netty.handler.codec.mqtt.MqttVersion;
import io.smallrye.mutiny.Uni;
import io.vertx.core.json.JsonObject;
import io.vertx.mqtt.messages.codes.MqttPubAckReasonCode;
import io.vertx.mqtt.messages.codes.MqttPubRecReasonCode;
import io.vertx.mutiny.core.Vertx;
import io.vertx.mutiny.mqtt.MqttEndpoint;
import io.vertx.mutiny.mqtt.messages.MqttPublishMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
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

  public MqttPublishHandler(MqttEndpoint mqttEndpoint, Vertx vertx, MsgService msgService, SessionService sessionService,
                            RetainService retainService, CompositeService compositeService, EventService eventService) {
    this.mqttEndpoint = mqttEndpoint;
    this.vertx = vertx;
    this.msgService = msgService;
    this.sessionService = sessionService;
    this.retainService = retainService;
    this.compositeService = compositeService;
    this.eventService = eventService;
  }

  @Override
  public void accept(MqttPublishMessage mqttPublishMessage) {
    if (LOGGER.isDebugEnabled()){
      LOGGER.debug("PUBLISH from {}: {}", mqttEndpoint.clientIdentifier(), publicationInfo(mqttPublishMessage));
    }

    sessionService.updateLatestUpdatedTime(mqttEndpoint.clientIdentifier(), Instant.now().toEpochMilli())
      .subscribe().with(ConsumerUtil.nothingToDo(), t -> LOGGER.error("Error occurred when updating session latest updatedTime", t));

    MqttProperties pubAckProperties = new MqttProperties();
    MqttProperties pubRecProperties = new MqttProperties();
    Uni.createFrom().voidItem()
      .onItem().transformToUni(v -> checkTopic(mqttPublishMessage))
      .onItem().transformToUni(v -> authorize(mqttPublishMessage))
      .onItem().transformToUni(v -> handleQos(mqttPublishMessage))
      .onItem().call(v -> sessionService.getSession(mqttEndpoint.clientIdentifier())
        .onItem().transformToUni(session -> publishEvent(session, mqttPublishMessage)))
      .subscribe().with(v -> {
        if (LOGGER.isDebugEnabled()){
          LOGGER.debug("PUBLISH from {} to {} accepted", mqttEndpoint.clientIdentifier(), mqttPublishMessage.topicName());
        }
        if (mqttEndpoint.protocolVersion() <= MqttVersion.MQTT_3_1_1.protocolLevel()) {
          switch (mqttPublishMessage.qosLevel()) {
            case AT_MOST_ONCE -> {
            }
            case AT_LEAST_ONCE -> mqttEndpoint.publishAcknowledge(mqttPublishMessage.messageId());
            case EXACTLY_ONCE -> mqttEndpoint.publishReceived(mqttPublishMessage.messageId());
          }
        } else {
          switch (mqttPublishMessage.qosLevel()) {
            case AT_MOST_ONCE -> {
            }
            case AT_LEAST_ONCE ->
              mqttEndpoint.publishAcknowledge(mqttPublishMessage.messageId(), MqttPubAckReasonCode.SUCCESS, pubAckProperties);
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
          if (t instanceof MqttPublishException) {
            switch (mqttPublishMessage.qosLevel()) {
              case AT_MOST_ONCE:
                break;
              case AT_LEAST_ONCE:
                mqttEndpoint.publishAcknowledge(mqttPublishMessage.messageId(), ((MqttPublishException) t).mqttPubAckReasonCode(), pubAckProperties);
                break;
              case EXACTLY_ONCE:
                mqttEndpoint.publishReceived(mqttPublishMessage.messageId(), ((MqttPublishException) t).mqttPubRecReasonCode(), pubRecProperties);
            }
          } else {
            switch (mqttPublishMessage.qosLevel()) {
              case AT_MOST_ONCE:
                break;
              case AT_LEAST_ONCE:
                pubAckProperties.add(new MqttProperties.StringProperty(MqttProperties.MqttPropertyType.REASON_STRING.value(), t.getMessage()));
                mqttEndpoint.publishAcknowledge(mqttPublishMessage.messageId(), MqttPubAckReasonCode.UNSPECIFIED_ERROR, pubAckProperties);
                break;
              case EXACTLY_ONCE:
                pubRecProperties.add(new MqttProperties.StringProperty(MqttProperties.MqttPropertyType.REASON_STRING.value(), t.getMessage()));
                mqttEndpoint.publishReceived(mqttPublishMessage.messageId(), MqttPubRecReasonCode.UNSPECIFIED_ERROR, pubRecProperties);
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
   * @return Void
   */
  private Uni<Void> handleQos(MqttPublishMessage mqttPublishMessage) {
    return switch (mqttPublishMessage.qosLevel()) {
      case AT_MOST_ONCE, AT_LEAST_ONCE ->
        // Nothing need to do.
        Uni.createFrom().voidItem();
      case EXACTLY_ONCE -> sessionService.getSession(mqttEndpoint.clientIdentifier())
        .onItem().transformToUni(session -> {
          InboundQos2Pub inboundQos2Pub = new InboundQos2Pub(session.getSessionId(), mqttEndpoint.clientIdentifier(),
            mqttPublishMessage.messageId(), mqttPublishMessage.topicName(), mqttPublishMessage.qosLevel().value(),
            mqttPublishMessage.payload().getDelegate(), mqttPublishMessage.isDup(), mqttPublishMessage.isRetain(),
            Instant.now().toEpochMilli());
          return msgService.saveInboundQos2Pub(inboundQos2Pub);
        });
      default -> Uni.createFrom().failure(new MqttPublishException("Unknown mqtt qos"));
    };
  }

  private Uni<Void> publishEvent(Session session, MqttPublishMessage mqttPublishMessage){
    Event event = new MqttPublishInboundAcceptedEvent(Instant.now().toEpochMilli(), VertxUtil.getNodeId(vertx),
      mqttEndpoint.clientIdentifier(), session.getSessionId(), mqttPublishMessage.topicName(), mqttPublishMessage.qosLevel().value(),
      mqttPublishMessage.messageId(), mqttPublishMessage.payload().getDelegate(), mqttPublishMessage.isDup(), mqttPublishMessage.isRetain());
    if (LOGGER.isDebugEnabled()){
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
        Retain retainMessage = new Retain(mqttPublishMessage.topicName(), mqttPublishMessage.qosLevel().value(),
          mqttPublishMessage.payload().getDelegate(), Instant.now().toEpochMilli());
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
