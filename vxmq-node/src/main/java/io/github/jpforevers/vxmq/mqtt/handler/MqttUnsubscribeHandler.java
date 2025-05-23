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
import io.github.jpforevers.vxmq.assist.VertxUtil;
import io.github.jpforevers.vxmq.event.Event;
import io.github.jpforevers.vxmq.event.EventService;
import io.github.jpforevers.vxmq.event.mqtt.MqttUnsubscribedEvent;
import io.github.jpforevers.vxmq.assist.MqttPropertiesUtil;
import io.github.jpforevers.vxmq.assist.TopicUtil;
import io.github.jpforevers.vxmq.mqtt.exception.MqttUnsubscribeException;
import io.github.jpforevers.vxmq.service.session.Session;
import io.github.jpforevers.vxmq.service.session.SessionService;
import io.github.jpforevers.vxmq.service.sub.mutiny.SubService;
import io.netty.handler.codec.mqtt.MqttProperties;
import io.netty.handler.codec.mqtt.MqttVersion;
import io.smallrye.mutiny.Uni;
import io.vertx.core.json.JsonObject;
import io.vertx.mqtt.messages.codes.MqttUnsubAckReasonCode;
import io.vertx.mutiny.core.Vertx;
import io.vertx.mutiny.mqtt.MqttEndpoint;
import io.vertx.mutiny.mqtt.messages.MqttUnsubscribeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * This handler is called when a UNSUBSCRIBE message is received.
 */
public class MqttUnsubscribeHandler implements Consumer<MqttUnsubscribeMessage> {

  private static final Logger LOGGER = LoggerFactory.getLogger(MqttUnsubscribeHandler.class);

  private final MqttEndpoint mqttEndpoint;
  private final Vertx vertx;
  private final SessionService sessionService;
  private final SubService subService;
  private final EventService eventService;

  public MqttUnsubscribeHandler(MqttEndpoint mqttEndpoint, Vertx vertx, SessionService sessionService, SubService subService, EventService eventService) {
    this.mqttEndpoint = mqttEndpoint;
    this.vertx = vertx;
    this.sessionService = sessionService;
    this.subService = subService;
    this.eventService = eventService;
  }

  @Override
  public void accept(MqttUnsubscribeMessage mqttUnsubscribeMessage) {
    if (LOGGER.isDebugEnabled()){
      LOGGER.debug("UNSUBSCRIBE from {}: {}", mqttEndpoint.clientIdentifier(), unsubscribeInfo(mqttUnsubscribeMessage));
    }

    sessionService.updateLatestUpdatedTime(mqttEndpoint.clientIdentifier(), Instant.now().toEpochMilli())
      .subscribe().with(ConsumerUtil.nothingToDo(), t -> LOGGER.error("Error occurred when updating session latest updatedTime", t));

    MqttProperties unsubAckProperties = new MqttProperties();
    List<MqttUnsubAckReasonCode> reasonCodes = new ArrayList<>();
    processingUnsubsRecursively(0, mqttUnsubscribeMessage, reasonCodes, unsubAckProperties)
      .onItemOrFailure().invoke((v, t) -> {
        if (mqttEndpoint.protocolVersion() <= MqttVersion.MQTT_3_1_1.protocolLevel()) {
          mqttEndpoint.unsubscribeAcknowledge(mqttUnsubscribeMessage.messageId());
        } else {
          mqttEndpoint.unsubscribeAcknowledge(mqttUnsubscribeMessage.messageId(), reasonCodes, unsubAckProperties);
        }
      })
      .subscribe().with(ConsumerUtil.nothingToDo(), ConsumerUtil.nothingToDo());
  }

  private Uni<Void> processingUnsubsRecursively(int index, MqttUnsubscribeMessage mqttUnsubscribeMessage, List<MqttUnsubAckReasonCode> reasonCodes, MqttProperties unsubAckProperties) {
    if (index < mqttUnsubscribeMessage.topics().size()) {
      String topicUnSub = mqttUnsubscribeMessage.topics().get(index);
      return Uni.createFrom().voidItem()
        .onItem().transformToUni(v -> checkTopicFilter(topicUnSub))
        .onItem().transformToUni(v -> authorize(mqttEndpoint.clientIdentifier(), topicUnSub))
        .onItem().transformToUni(v -> sessionService.getSessionByFields(mqttEndpoint.clientIdentifier(), new Session.Field[]{Session.Field.sessionId}))
        .onItem().call(sessionFields -> removeSub((String) sessionFields.get(Session.Field.sessionId), topicUnSub))
        .onItem().invoke(() -> {
          if (mqttEndpoint.protocolVersion() <= MqttVersion.MQTT_3_1_1.protocolLevel()) {
            // http://docs.oasis-open.org/mqtt/mqtt/v3.1.1/errata01/os/mqtt-v3.1.1-errata01-os-complete.html#_Toc442180893
            // The UNSUBACK Packet has no payload. Nothing need to do.
          } else {
            reasonCodes.add(MqttUnsubAckReasonCode.SUCCESS);
          }
          if (LOGGER.isDebugEnabled()){
            LOGGER.debug("UNSUBSCRIBE from {} to {} accepted", mqttEndpoint.clientIdentifier(), topicUnSub);
          }
        })
        .onItem().call(sessionFields -> publishEvent((String) sessionFields.get(Session.Field.sessionId), topicUnSub))
        .onFailure().invoke(t -> {
          LOGGER.error("Error occurred when processing UNSUBSCRIBE from {} to {}", mqttEndpoint.clientIdentifier(), topicUnSub, t);
          if (mqttEndpoint.protocolVersion() <= MqttVersion.MQTT_3_1_1.protocolLevel()) {
            // http://docs.oasis-open.org/mqtt/mqtt/v3.1.1/errata01/os/mqtt-v3.1.1-errata01-os-complete.html#_Toc442180893
            // The UNSUBACK Packet has no payload. Nothing need to do.
          } else {
            Integer requestProblemInformation = MqttPropertiesUtil.getValue(mqttEndpoint.connectProperties(), MqttProperties.MqttPropertyType.REQUEST_PROBLEM_INFORMATION, MqttProperties.IntegerProperty.class);
            if (requestProblemInformation == null || requestProblemInformation == 1) {
              unsubAckProperties.add(new MqttProperties.StringProperty(MqttProperties.MqttPropertyType.REASON_STRING.value(), t.getMessage()));
            }
            if (t instanceof MqttUnsubscribeException) {
              reasonCodes.add(((MqttUnsubscribeException) t).getMqttUnsubAckReasonCode());
            } else {
              reasonCodes.add(MqttUnsubAckReasonCode.UNSPECIFIED_ERROR);
            }
          }
        })
        .onItem().transformToUni(v -> processingUnsubsRecursively(index + 1, mqttUnsubscribeMessage, reasonCodes, unsubAckProperties));
    } else {
      return Uni.createFrom().voidItem();
    }
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
        return Uni.createFrom().failure(new MqttUnsubscribeException("Topic filter invalid"));
      } else {
        return Uni.createFrom().failure(new MqttUnsubscribeException(MqttUnsubAckReasonCode.TOPIC_FILTER_INVALID));
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
        return Uni.createFrom().failure(new MqttUnsubscribeException("Not authorized"));
      } else {
        return Uni.createFrom().failure(new MqttUnsubscribeException(MqttUnsubAckReasonCode.NOT_AUTHORIZED));
      }
    }
  }

  private String unsubscribeInfo(MqttUnsubscribeMessage mqttUnsubscribeMessage) {
    JsonObject jsonObject = new JsonObject();
    jsonObject.put("topics", mqttUnsubscribeMessage.topics());
    jsonObject.put("unsubscribeProperties", MqttPropertiesUtil.encode(mqttUnsubscribeMessage.properties()));
    return jsonObject.toString();
  }

  /**
   * Remove subscription
   *
   * @param sessionId   sessionId
   * @param topicFilter topicFilter
   * @return Void
   */
  private Uni<Void> removeSub(String sessionId, String topicFilter) {
    return subService.removeSub(sessionId, topicFilter)
      .onItem().transformToUni(absent -> absent ? Uni.createFrom().voidItem() : Uni.createFrom().failure(new MqttUnsubscribeException(MqttUnsubAckReasonCode.NO_SUBSCRIPTION_EXISTED)));
  }

  private Uni<Void> publishEvent(String sessionId, String topicUnSub){
    Event event = new MqttUnsubscribedEvent(Instant.now().toEpochMilli(), VertxUtil.getNodeId(vertx),
      mqttEndpoint.clientIdentifier(), sessionId, topicUnSub);
    if (LOGGER.isDebugEnabled()){
      LOGGER.debug("Publishing event: {}, ", event.toJson());
    }
    return eventService.publishEvent(event);
  }

}
