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
import cloud.wangyongjun.vxmq.event.mqtt.MqttPingEvent;
import cloud.wangyongjun.vxmq.service.session.SessionService;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.core.Vertx;
import io.vertx.mutiny.mqtt.MqttEndpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;

/**
 * This handler is called when a PINGREQ message is received by the remote MQTT client. In any case the endpoint sends the PINGRESP internally after executing this handler.
 */
public class MqttPingHandler implements Runnable {

  private final static Logger LOGGER = LoggerFactory.getLogger(MqttPingHandler.class);

  private final MqttEndpoint mqttEndpoint;
  private final Vertx vertx;
  private final SessionService sessionService;
  private final EventService eventService;

  public MqttPingHandler(MqttEndpoint mqttEndpoint, Vertx vertx, SessionService sessionService, EventService eventService) {
    this.mqttEndpoint = mqttEndpoint;
    this.vertx = vertx;
    this.sessionService = sessionService;
    this.eventService = eventService;
  }

  @Override
  public void run() {
    if (LOGGER.isDebugEnabled()){
      LOGGER.debug("PINGREQ from {}", mqttEndpoint.clientIdentifier());
    }
    String clientId = mqttEndpoint.clientIdentifier();
    Uni.createFrom().voidItem()
      .onItem().transformToUni(v -> sessionService.updateLatestUpdatedTime(clientId, Instant.now().toEpochMilli()))
      .onItem().transformToUni(v -> publishEvent(mqttEndpoint))
      .subscribe().with(ConsumerUtil.nothingToDo(), t -> LOGGER.error("Error occurred when processing the PINGREQ from " + clientId, t));
  }

  private Uni<Void> publishEvent(MqttEndpoint mqttEndpoint){
    Event event = new MqttPingEvent(Instant.now().toEpochMilli(), VertxUtil.getNodeId(vertx), mqttEndpoint.clientIdentifier());
    if (LOGGER.isDebugEnabled()){
      LOGGER.debug("Publishing event: {}, ", event.toJson());
    }
    return eventService.publishEvent(event);
  }

}
