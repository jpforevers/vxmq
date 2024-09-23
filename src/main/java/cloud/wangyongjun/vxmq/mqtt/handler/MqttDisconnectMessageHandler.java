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

package cloud.wangyongjun.vxmq.mqtt.handler;

import cloud.wangyongjun.vxmq.assist.VertxUtil;
import cloud.wangyongjun.vxmq.assist.MqttPropertiesUtil;
import cloud.wangyongjun.vxmq.event.Event;
import cloud.wangyongjun.vxmq.event.EventService;
import cloud.wangyongjun.vxmq.event.mqtt.MqttDisconnectedEvent;
import cloud.wangyongjun.vxmq.service.session.Session;
import cloud.wangyongjun.vxmq.service.session.SessionService;
import cloud.wangyongjun.vxmq.service.will.WillService;
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
    if (LOGGER.isDebugEnabled()){
      LOGGER.debug("DISCONNECT from {}: {}", mqttEndpoint.clientIdentifier(), disconnectInfo(mqttDisconnectMessage));
    }
    sessionService.getSession(mqttEndpoint.clientIdentifier())
      .onItem().call(session -> handleWill(session.getProtocolLevel(), session.getSessionId(), mqttDisconnectMessage.code()))
      .onItem().call(session -> processSessionExpiryInterval(mqttDisconnectMessage, session))
      // Publish EVENT_MQTT_DISCONNECTED_EVENT
      .onItem().call(session -> publishEvent(mqttEndpoint, session, mqttDisconnectMessage))
      .subscribe().with(v -> {
        if (LOGGER.isDebugEnabled()){
          LOGGER.debug("Mqtt client {} disconnected", mqttEndpoint.clientIdentifier());
        }}, t -> LOGGER.error("Error occurred when processing DISCONNECT from {}", mqttEndpoint.clientIdentifier(), t));
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

  /**
   * <a href="https://docs.oasis-open.org/mqtt/mqtt/v5.0/os/mqtt-v5.0-os.html#_Toc3901211">Session expiry in DISCONNECT in MQTT5 specification</a>
   */
  private Uni<Void> processSessionExpiryInterval(MqttDisconnectMessage mqttDisconnectMessage, Session session) {
    if (session.getProtocolLevel() <= MqttVersion.MQTT_3_1_1.protocolLevel()) {
      return Uni.createFrom().voidItem();
    } else {
      MqttProperties.MqttProperty sessionExpiryIntervalProperty = mqttDisconnectMessage.properties().getProperty(MqttProperties.MqttPropertyType.SESSION_EXPIRY_INTERVAL.value());
      Integer sessionExpiryInterval = sessionExpiryIntervalProperty == null ? null : (Integer) sessionExpiryIntervalProperty.value();
      if (session.getSessionExpiryInterval() != null && session.getSessionExpiryInterval() == 0 && sessionExpiryInterval != null && sessionExpiryInterval != 0) {
        // From MQTT 5 specification: If the Session Expiry Interval in the CONNECT packet was zero, then it is a Protocol Error to set a non-zero Session Expiry Interval in the DISCONNECT packet sent by the Client
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

  private Uni<Void> publishEvent(MqttEndpoint mqttEndpoint, Session session, MqttDisconnectMessage mqttDisconnectMessage){
    Event event = new MqttDisconnectedEvent(Instant.now().toEpochMilli(), VertxUtil.getNodeId(vertx),
      mqttEndpoint.clientIdentifier(), session.getSessionId(), mqttDisconnectMessage.code());
    if (LOGGER.isDebugEnabled()){
      LOGGER.debug("Publishing event: {}, ", event.toJson());
    }
    return eventService.publishEvent(event);
  }

}
