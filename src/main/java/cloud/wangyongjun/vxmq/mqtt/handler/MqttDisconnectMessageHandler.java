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

import cloud.wangyongjun.vxmq.assist.VertxUtil;
import cloud.wangyongjun.vxmq.mqtt.MqttPropertiesUtil;
import cloud.wangyongjun.vxmq.event.EventService;
import cloud.wangyongjun.vxmq.event.MqttDisconnectedEvent;
import cloud.wangyongjun.vxmq.mqtt.session.Session;
import cloud.wangyongjun.vxmq.mqtt.session.SessionService;
import cloud.wangyongjun.vxmq.mqtt.will.WillService;
import io.netty.handler.codec.mqtt.MqttProperties;
import io.netty.handler.codec.mqtt.MqttVersion;
import io.smallrye.mutiny.Uni;
import io.vertx.core.json.JsonObject;
import io.vertx.mqtt.messages.codes.MqttDisconnectReasonCode;
import io.vertx.mutiny.core.Vertx;
import io.vertx.mutiny.mqtt.MqttEndpoint;
import io.vertx.mutiny.mqtt.messages.MqttDisconnectMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.function.Consumer;

/**
 * This handler is called when a DISCONNECT message is received by the remote MQTT client.
 */
public class MqttDisconnectMessageHandler implements Consumer<MqttDisconnectMessage> {

  private final static Logger LOGGER = LoggerFactory.getLogger(MqttDisconnectMessageHandler.class);

  private final MqttEndpoint mqttEndpoint;
  private final Vertx vertx;
  private final SessionService sessionService;
  private final WillService willService;
  private final EventService eventService;

  public MqttDisconnectMessageHandler(MqttEndpoint mqttEndpoint, Vertx vertx, SessionService sessionService, WillService willService, EventService eventService) {
    this.mqttEndpoint = mqttEndpoint;
    this.vertx = vertx;
    this.sessionService = sessionService;
    this.willService = willService;
    this.eventService = eventService;
  }

  @Override
  public void accept(MqttDisconnectMessage mqttDisconnectMessage) {
    LOGGER.debug("DISCONNECT from {}: {}", mqttEndpoint.clientIdentifier(), disconnectInfo(mqttDisconnectMessage));
    sessionService.getSession(mqttEndpoint.clientIdentifier())
      .onItem().call(session -> handleWill(session.getProtocolLevel(), session.getSessionId(), mqttDisconnectMessage.code()))
      .onItem().call(session -> processSessionExpiryInterval(mqttDisconnectMessage, session))
      // Publish EVENT_MQTT_DISCONNECTED_EVENT
      .onItem().call(session -> eventService.publishEvent(new MqttDisconnectedEvent(Instant.now().toEpochMilli(), VertxUtil.getNodeId(vertx),
        mqttEndpoint.clientIdentifier(), session.getSessionId(), mqttDisconnectMessage.code())))
      .subscribe().with(v -> LOGGER.debug("Mqtt client {} disconnected", mqttEndpoint.clientIdentifier()),
        t -> LOGGER.error("Error occurred when processing DISCONNECT from " + mqttEndpoint.clientIdentifier(), t));
  }

  private String disconnectInfo(MqttDisconnectMessage mqttDisconnectMessage) {
    JsonObject jsonObject = new JsonObject();
    jsonObject.put("code", mqttDisconnectMessage.code());
    jsonObject.put("properties", MqttPropertiesUtil.encode(mqttDisconnectMessage.properties()));
    return jsonObject.toString();
  }

  private Uni<Void> handleWill(int protocolLevel, String sessionId, MqttDisconnectReasonCode disconnectReasonCode) {
    if (protocolLevel <= MqttVersion.MQTT_3_1_1.protocolLevel()) {
      return willService.removeWill(sessionId);
    } else {
      if (disconnectReasonCode.equals(MqttDisconnectReasonCode.NORMAL)) {
        return willService.removeWill(sessionId);
      } else {
        return Uni.createFrom().voidItem();
      }
    }
  }

  private Uni<Void> processSessionExpiryInterval(MqttDisconnectMessage mqttDisconnectMessage, Session session) {
    if (session.getProtocolLevel() <= MqttVersion.MQTT_3_1_1.protocolLevel()) {
      return Uni.createFrom().voidItem();
    } else {
      MqttProperties.MqttProperty sessionExpiryIntervalProperty = mqttDisconnectMessage.properties().getProperty(MqttProperties.MqttPropertyType.SESSION_EXPIRY_INTERVAL.value());
      Integer sessionExpiryInterval = sessionExpiryIntervalProperty == null ? null : (Integer) sessionExpiryIntervalProperty.value();
      if (session.getSessionExpiryInterval() != null && session.getSessionExpiryInterval() == 0 && sessionExpiryInterval != null && sessionExpiryInterval != 0) {
        return Uni.createFrom().voidItem();
      } else {
        if (sessionExpiryInterval != null) {
          session.setSessionExpiryInterval(sessionExpiryInterval);
          return sessionService.saveOrUpdateSession(session);
        } else {
          return Uni.createFrom().voidItem();
        }
      }
    }
  }

}
