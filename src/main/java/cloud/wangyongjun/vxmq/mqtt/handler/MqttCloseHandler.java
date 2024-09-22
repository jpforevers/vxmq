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
import cloud.wangyongjun.vxmq.event.mqtt.MqttEndpointClosedEvent;
import cloud.wangyongjun.vxmq.service.alias.InboundTopicAliasService;
import cloud.wangyongjun.vxmq.service.client.ClientService;
import cloud.wangyongjun.vxmq.service.composite.CompositeService;
import cloud.wangyongjun.vxmq.service.session.Session;
import cloud.wangyongjun.vxmq.service.session.SessionService;
import cloud.wangyongjun.vxmq.service.will.WillService;
import io.netty.handler.codec.mqtt.MqttVersion;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.core.Vertx;
import io.vertx.mutiny.mqtt.MqttEndpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;

/**
 * This will be called when the MQTT endpoint is closed.
 */
public class MqttCloseHandler implements Runnable {

  private final static Logger LOGGER = LoggerFactory.getLogger(MqttCloseHandler.class);

  private final MqttEndpoint mqttEndpoint;
  private final Vertx vertx;
  private final ClientService clientService;
  private final CompositeService compositeService;
  private final SessionService sessionService;
  private final WillService willService;
  private final EventService eventService;
  private final InboundTopicAliasService inboundTopicAliasService;

  public MqttCloseHandler(MqttEndpoint mqttEndpoint, Vertx vertx,
                          ClientService clientService,
                          CompositeService compositeService,
                          SessionService sessionService,
                          WillService willService, EventService eventService,
                          InboundTopicAliasService inboundTopicAliasService) {
    this.mqttEndpoint = mqttEndpoint;
    this.vertx = vertx;
    this.clientService = clientService;
    this.compositeService = compositeService;
    this.sessionService = sessionService;
    this.willService = willService;
    this.eventService = eventService;
    this.inboundTopicAliasService = inboundTopicAliasService;
  }

  @Override
  public void run() {
    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("Mqtt endpoint of client {} closed", mqttEndpoint.clientIdentifier());
    }

    Uni.createFrom().voidItem()
      .onItem().transformToUni(v -> clientService.obtainClientLock(mqttEndpoint.clientIdentifier(), 2000))
      .onItem().transformToUni(v -> sessionService.getSession(mqttEndpoint.clientIdentifier()))
      .onItem().transformToUni(session -> {
        if (session != null) {
          return Uni.createFrom().voidItem()
            .onItem().transformToUni(v -> handleWill(session))
            .onItem().transformToUni(v -> undeployClientVerticle(session))
            .onItem().transformToUni(v -> handleSession(session))
            // Publish EVENT_MQTT_ENDPOINT_CLOSED_EVENT
            .onItem().call(v -> publishEvent(mqttEndpoint, session));
        } else {
          return Uni.createFrom().voidItem();
        }
      })
      .onItem().invoke(() -> inboundTopicAliasService.clearTopicAlias(mqttEndpoint.clientIdentifier()))
      .eventually(() -> clientService.releaseClientLock(mqttEndpoint.clientIdentifier()))
      .subscribe().with(ConsumerUtil.nothingToDo(), t -> LOGGER.error("Error occurred when processing MQTT endpoint close", t));
  }

  /**
   * Publish will.
   *
   * @param session session
   * @return Void
   */
  public Uni<Void> handleWill(Session session) {
    return willService.getWill(session.getSessionId())
      .onItem().transformToUni(will -> {
        if (will != null) {
          if (mqttEndpoint.protocolVersion() > MqttVersion.MQTT_3_1_1.protocolLevel()) {
            if (will.getWillDelayInterval() == null || will.getWillDelayInterval() == 0) {
              return compositeService.publishWill(session.getSessionId());
            } else {
              vertx.setTimer(will.getWillDelayInterval() * 1000, l -> sessionService.getSession(session.getClientId())
                .onItem().transformToUni(sessionNow -> {
                  if (sessionNow.isOnline()) {
                    // If a new Network Connection to this Session is made before the Will Delay Interval has passed, the Server MUST NOT send the Will Message
                    return Uni.createFrom().voidItem();
                  } else {
                    return compositeService.publishWill(session.getSessionId());
                  }
                })
                .subscribe().with(ConsumerUtil.nothingToDo(), t -> LOGGER.error("Error occurred when publish will", t)));
              return Uni.createFrom().voidItem();
            }
          } else {
            return compositeService.publishWill(session.getSessionId());
          }
        } else {
          return Uni.createFrom().voidItem();
        }
      });
  }

  /**
   * Undeploy client verticle
   *
   * @param session session
   * @return Void
   */
  private Uni<Void> undeployClientVerticle(Session session) {
    return clientService.undeployClientVerticle(session.getVerticleId());
  }

  /**
   * Handle session
   *
   * @param session session
   * @return Void
   */
  private Uni<Void> handleSession(Session session) {
    if (session != null) {
      if (session.getProtocolLevel() <= MqttVersion.MQTT_3_1_1.protocolLevel()) {
        if (session.isCleanSession()) {
          return compositeService.clearSessionData(session.getClientId());
        } else {
          return sessionService.saveOrUpdateSession(session.copy().setOnline(false).setVerticleId(null).setNodeId(null).setUpdatedTime(Instant.now().toEpochMilli()));
        }
      } else {
        if (session.getSessionExpiryInterval() == null || session.getSessionExpiryInterval() == 0) {
          return compositeService.clearSessionData(session.getClientId())
            .onItem().transformToUni(v -> compositeService.publishWill(session.getSessionId()));
        } else {
          // From MQTT 5 specification: If the Session Expiry Interval is 0xFFFFFFFF (UINT_MAX), the Session does not expire.
          // The result of Integer.valueOf(0xFFFFFFFF) is -1.
          if (session.getSessionExpiryInterval() != -1) {
            vertx.setTimer(session.getSessionExpiryInterval() * 1000, l -> sessionService.getSession(session.getClientId())
              .onItem().transformToUni(sessionX -> {
                if (sessionX.isOnline()) {
                  return Uni.createFrom().voidItem();
                } else {
                  return compositeService.clearSessionData(sessionX.getClientId())
                    .onItem().transformToUni(v -> compositeService.publishWill(sessionX.getSessionId()));
                }
              })
              .subscribe().with(ConsumerUtil.nothingToDo(), t -> LOGGER.error("Error occurred when clear session on session expiry interval passed")));
          }
          return sessionService.saveOrUpdateSession(session.copy().setOnline(false).setVerticleId(null).setNodeId(null).setUpdatedTime(Instant.now().toEpochMilli()));
        }
      }
    } else {
      return Uni.createFrom().voidItem();
    }
  }

  public Uni<Void> publishEvent(MqttEndpoint mqttEndpoint, Session session){
    Event event = new MqttEndpointClosedEvent(Instant.now().toEpochMilli(), VertxUtil.getNodeId(vertx), mqttEndpoint.clientIdentifier(), session.getSessionId());
    if (LOGGER.isDebugEnabled()){
      LOGGER.debug("Publishing event: {}, ", event.toJson());
    }
    return eventService.publishEvent(event);
  }

}
