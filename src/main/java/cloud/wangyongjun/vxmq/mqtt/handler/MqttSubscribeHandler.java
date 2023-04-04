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
import cloud.wangyongjun.vxmq.event.EventService;
import cloud.wangyongjun.vxmq.event.MqttSubscribedEvent;
import cloud.wangyongjun.vxmq.mqtt.MqttPropertiesUtil;
import cloud.wangyongjun.vxmq.mqtt.TopicUtil;
import cloud.wangyongjun.vxmq.mqtt.composite.CompositeService;
import cloud.wangyongjun.vxmq.mqtt.exception.MqttSubscribeException;
import cloud.wangyongjun.vxmq.mqtt.msg.MsgToClient;
import cloud.wangyongjun.vxmq.mqtt.retain.Retain;
import cloud.wangyongjun.vxmq.mqtt.retain.RetainService;
import cloud.wangyongjun.vxmq.mqtt.session.SessionService;
import cloud.wangyongjun.vxmq.mqtt.sub.Subscription;
import cloud.wangyongjun.vxmq.mqtt.sub.mutiny.SubService;
import io.netty.handler.codec.mqtt.MqttProperties;
import io.netty.handler.codec.mqtt.MqttQoS;
import io.netty.handler.codec.mqtt.MqttSubscriptionOption;
import io.netty.handler.codec.mqtt.MqttVersion;
import io.smallrye.mutiny.Uni;
import io.vertx.core.json.JsonObject;
import io.vertx.mqtt.messages.codes.MqttSubAckReasonCode;
import io.vertx.mutiny.core.Vertx;
import io.vertx.mutiny.mqtt.MqttEndpoint;
import io.vertx.mutiny.mqtt.MqttTopicSubscription;
import io.vertx.mutiny.mqtt.messages.MqttSubscribeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * This handler is called when a SUBSCRIBE message is received.
 */
@SuppressWarnings("ALL")
public class MqttSubscribeHandler implements Consumer<MqttSubscribeMessage> {

  private static final Logger LOGGER = LoggerFactory.getLogger(MqttSubscribeHandler.class);

  private final MqttEndpoint mqttEndpoint;
  private final Vertx vertx;
  private final SubService subService;
  private final SessionService sessionService;
  private final RetainService retainService;
  private final CompositeService compositeService;
  private final EventService eventService;

  public MqttSubscribeHandler(MqttEndpoint mqttEndpoint, Vertx vertx, SubService subService, SessionService sessionService,
                              RetainService retainService, CompositeService compositeService, EventService eventService) {
    this.mqttEndpoint = mqttEndpoint;
    this.vertx = vertx;
    this.subService = subService;
    this.sessionService = sessionService;
    this.retainService = retainService;
    this.compositeService = compositeService;
    this.eventService = eventService;
  }

  @Override
  public void accept(MqttSubscribeMessage mqttSubscribeMessage) {
    String clientId = mqttEndpoint.clientIdentifier();
    LOGGER.debug("SUBSCRIBE from {}: {}", clientId, subscribeInfo(mqttSubscribeMessage));

    sessionService.updateLatestUpdatedTime(clientId, Instant.now().toEpochMilli())
      .subscribe().with(ConsumerUtil.nothingToDo(), t -> LOGGER.error("Error occurred when updating session latest updatedTime", t));

    MqttProperties subAckMqttProperties = new MqttProperties();
    List<MqttQoS> grantedQosLevels = new ArrayList<>();
    List<MqttSubAckReasonCode> reasonCodes = new ArrayList<>();
    processingSubsRecursively(0, mqttSubscribeMessage, subAckMqttProperties, grantedQosLevels, reasonCodes)
      .onItemOrFailure().invoke((v, t) -> {
        if (mqttEndpoint.protocolVersion() <= MqttVersion.MQTT_3_1_1.protocolLevel()) {
          mqttEndpoint.subscribeAcknowledge(mqttSubscribeMessage.messageId(), grantedQosLevels);
        } else {
          mqttEndpoint.subscribeAcknowledge(mqttSubscribeMessage.messageId(), reasonCodes, subAckMqttProperties);
        }
      })
      .subscribe().with(ConsumerUtil.nothingToDo(), ConsumerUtil.nothingToDo());
  }

  private Uni<Void> processingSubsRecursively(int index, MqttSubscribeMessage mqttSubscribeMessage, MqttProperties subAckMqttProperties,
                                              List<MqttQoS> grantedQosLevels, List<MqttSubAckReasonCode> reasonCodes) {
    if (index < mqttSubscribeMessage.topicSubscriptions().size()) {
      MqttTopicSubscription topicSubscription = mqttSubscribeMessage.topicSubscriptions().get(index);
      return Uni.createFrom().voidItem()
        .onItem().transformToUni(v -> checkTopicFilter(topicSubscription.topicName()))
        .onItem().transformToUni(v -> authorize(mqttEndpoint.clientIdentifier(), topicSubscription.topicName()))
        .onItem().transformToUni(v -> saveSub(mqttEndpoint.clientIdentifier(), topicSubscription.topicName(), topicSubscription.qualityOfService().value(), mqttSubscribeMessage.properties(), topicSubscription.subscriptionOption()))
        .onItem().transformToUni(ifSubscriptionAlreadyExist -> handleRetain(mqttEndpoint.clientIdentifier(), topicSubscription.topicName(), ifSubscriptionAlreadyExist, topicSubscription.subscriptionOption().retainHandling()))
        .onItem().invoke(() -> {
          if (mqttEndpoint.protocolVersion() <= MqttVersion.MQTT_3_1_1.protocolLevel()) {
            grantedQosLevels.add(topicSubscription.qualityOfService());
          } else {
            reasonCodes.add(MqttSubAckReasonCode.qosGranted(topicSubscription.qualityOfService()));
          }
          LOGGER.debug("SUBSCRIBE from {} to {} accepted", mqttEndpoint.clientIdentifier(), topicSubscription.topicName());
        })
        .onItem().call(() -> sessionService.getSession(mqttEndpoint.clientIdentifier())
          .onItem().transformToUni(session -> eventService.publishEvent(new MqttSubscribedEvent(Instant.now().toEpochMilli(), VertxUtil.getNodeId(vertx),
           mqttEndpoint.clientIdentifier(), session.getSessionId(), topicSubscription.topicName(), topicSubscription.qualityOfService().value()))))
        .onFailure().invoke(t -> {
          LOGGER.error("Error occurred when processing SUBSCRIBE from " + mqttEndpoint.clientIdentifier() + " to " + topicSubscription.topicName(), t);
          if (mqttEndpoint.protocolVersion() <= MqttVersion.MQTT_3_1_1.protocolLevel()) {
            grantedQosLevels.add(MqttQoS.FAILURE);
          } else {
            LOGGER.error("Error occurred when processing SUBSCRIBE from " + mqttEndpoint.clientIdentifier() + " to " + topicSubscription.topicName(), t);
            if (mqttEndpoint.protocolVersion() <= MqttVersion.MQTT_3_1_1.protocolLevel()) {
              grantedQosLevels.add(MqttQoS.FAILURE);
            } else {
              if (t instanceof MqttSubscribeException) {
                reasonCodes.add(((MqttSubscribeException) t).code());
              } else {
                reasonCodes.add(MqttSubAckReasonCode.UNSPECIFIED_ERROR);
                subAckMqttProperties.add(new MqttProperties.StringProperty(MqttProperties.MqttPropertyType.REASON_STRING.value(), t.getMessage()));
              }
            }
          }
        }).onItem().transformToUni(v -> processingSubsRecursively(index + 1, mqttSubscribeMessage, subAckMqttProperties, grantedQosLevels, reasonCodes));
    } else {
      return Uni.createFrom().voidItem();
    }
  }

  private String subscribeInfo(MqttSubscribeMessage mqttSubscribeMessage) {
    JsonObject result = new JsonObject();
    result.put("topicSubscriptions", mqttSubscribeMessage.topicSubscriptions().stream().map(mqttTopicSubscription -> {
      JsonObject jsonObject = new JsonObject();
      jsonObject.put("topicName", mqttTopicSubscription.topicName());
      jsonObject.put("qualityOfService", mqttTopicSubscription.qualityOfService());
      jsonObject.put("subscriptionOption", new JsonObject()
        .put("qos", mqttTopicSubscription.subscriptionOption().qos())
        .put("isNoLocal", mqttTopicSubscription.subscriptionOption().isNoLocal())
        .put("isRetainAsPublished", mqttTopicSubscription.subscriptionOption().isRetainAsPublished())
        .put("retainHandling", mqttTopicSubscription.subscriptionOption().retainHandling()));
      return jsonObject;
    }).collect(Collectors.toList()));
    result.put("properties", MqttPropertiesUtil.encode(mqttSubscribeMessage.properties()));
    return result.toString();
  }

  /**
   * Check topicFilter
   *
   * @param topicFilter topicFilter
   * @return Void
   */
  private Uni<Void> checkTopicFilter(String topicFilter) {
    if (TopicUtil.isValidToSubscribe(topicFilter)) {
      return Uni.createFrom().voidItem();
    } else {
      if (mqttEndpoint.protocolVersion() <= MqttVersion.MQTT_3_1_1.protocolLevel()) {
        return Uni.createFrom().failure(new MqttSubscribeException(MqttSubAckReasonCode.UNSPECIFIED_ERROR));
      } else {
        return Uni.createFrom().failure(new MqttSubscribeException(MqttSubAckReasonCode.TOPIC_FILTER_INVALID));
      }
    }
  }

  /**
   * Authorize
   *
   * @param clientId    clientId
   * @param topicFilter topicFilter
   * @return Void
   */
  private Uni<Void> authorize(String clientId, String topicFilter) {
    // TODO 认证机制
    boolean authorizationResult = true;
    if (authorizationResult) {
      return Uni.createFrom().voidItem();
    } else {
      if (mqttEndpoint.protocolVersion() <= MqttVersion.MQTT_3_1_1.protocolLevel()) {
        return Uni.createFrom().failure(new MqttSubscribeException(MqttSubAckReasonCode.UNSPECIFIED_ERROR));
      } else {
        return Uni.createFrom().failure(new MqttSubscribeException(MqttSubAckReasonCode.NOT_AUTHORIZED));
      }
    }
  }

  /**
   * Save subscription
   *
   * @param clientId           clientId
   * @param topicFilter        topicFilter
   * @param qos                qos
   * @param subProperties      subProperties
   * @param subscriptionOption subscriptionOption
   * @return If subscription already exist.
   */
  private Uni<Boolean> saveSub(String clientId, String topicFilter, int qos, MqttProperties subProperties, MqttSubscriptionOption subscriptionOption) {
    return sessionService.getSession(clientId).onItem().transformToUni(session -> {
      String shareName = null;
      String realTopic = topicFilter;
      if (TopicUtil.isSharedSubscriptionTopic(topicFilter)) {
        shareName = TopicUtil.getShareNameFromSharedSubTopicFilter(topicFilter);
        realTopic = TopicUtil.getRealTopicFromSharedSubTopicFilter(topicFilter);
      }
      Subscription subscription;
      if (session != null) {
        if (mqttEndpoint.protocolVersion() <= MqttVersion.MQTT_3_1_1.protocolLevel()) {
          subscription = new Subscription().setSessionId(session.getSessionId()).setClientId(clientId).setTopicFilter(realTopic).setQos(qos)
            .setShareName(shareName).setCreatedTime(Instant.now().toEpochMilli());
        } else {
          MqttProperties.MqttProperty subscriptionIdentifierProperty = subProperties.getProperty(MqttProperties.MqttPropertyType.SUBSCRIPTION_IDENTIFIER.value());
          Integer subscriptionIdentifier = subscriptionIdentifierProperty == null ? null : (Integer) subscriptionIdentifierProperty.value();
          subscription = new Subscription().setSessionId(session.getSessionId()).setClientId(clientId).setTopicFilter(realTopic).setQos(qos)
            .setNoLocal(subscriptionOption.isNoLocal()).setRetainAsPublished(subscriptionOption.isRetainAsPublished()).setRetainHandling(subscriptionOption.retainHandling().value())
            .setSubscriptionIdentifier(subscriptionIdentifier).setShareName(shareName).setCreatedTime(Instant.now().toEpochMilli());
        }
        return subService.saveOrUpdateSub(subscription);
      } else {
        return Uni.createFrom().failure(new MqttSubscribeException(MqttSubAckReasonCode.UNSPECIFIED_ERROR));
      }
    });
  }

  /**
   * Handle retain.
   *
   * @param clientId                   clientId
   * @param topicFilter                topicFilter
   * @param ifSubscriptionAlreadyExist ifSubscriptionAlreadyExist
   * @param retainedHandlingPolicy     retainedHandlingPolicy
   * @return Void
   */
  private Uni<Void> handleRetain(String clientId, String topicFilter, boolean ifSubscriptionAlreadyExist, MqttSubscriptionOption.RetainedHandlingPolicy retainedHandlingPolicy) {
    if (TopicUtil.isSharedSubscriptionTopic(topicFilter)) {
      // When a new Non‑shared Subscription is made, the last retained message, if any, on each matching topic name is sent to the Client as directed by the Retain Handling Subscription Option. These messages are sent with the RETAIN flag set to 1.
      return Uni.createFrom().voidItem();
    } else {
      return sessionService.getSession(clientId)
        .onItem().transformToUni(session -> retainService.allTopicMatchRetains(topicFilter)
          .onItem().transformToUni(retains -> {
            List<Uni<Void>> unis = new ArrayList<>();
            for (Retain retain : retains) {
              // Bits 4 and 5 of the Subscription Options represent the Retain Handling option. This option specifies whether retained messages are sent when the subscription is established. This does not affect the sending of retained messages at any point after the subscribe. If there are no retained messages matching the Topic Filter, all of these values act the same. The values are:
              // 0 = Send retained messages at the time of the subscribe
              // 1 = Send retained messages at subscribe only if the subscription does not currently exist
              // 2 = Do not send retained messages at the time of the subscribe
              switch (retainedHandlingPolicy) {
                case SEND_AT_SUBSCRIBE:
                  unis.add(compositeService.sendToClient(session, new MsgToClient()
                    .setSessionId(session.getSessionId())
                    .setClientId(clientId).setTopic(retain.getTopicName()).setQos(retain.getQos()).setPayload(retain.getPayload()).setDup(false)
                    .setRetain(true).setCreatedTime(Instant.now().toEpochMilli())));
                  break;
                case SEND_AT_SUBSCRIBE_IF_NOT_YET_EXISTS:
                  if (!ifSubscriptionAlreadyExist) {
                    unis.add(compositeService.sendToClient(session, new MsgToClient().setSessionId(session.getSessionId())
                      .setClientId(clientId).setTopic(retain.getTopicName()).setQos(retain.getQos()).setPayload(retain.getPayload()).setDup(false)
                      .setRetain(true).setCreatedTime(Instant.now().toEpochMilli())));
                  }
                  break;
                case DONT_SEND_AT_SUBSCRIBE:
                  // Nothing to do.
              }
            }
            return unis.size() > 0 ? Uni.combine().all().unis(unis).discardItems() : Uni.createFrom().voidItem();
          }));
    }
  }

}
