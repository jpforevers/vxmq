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
import cloud.wangyongjun.vxmq.event.Event;
import cloud.wangyongjun.vxmq.event.EventService;
import cloud.wangyongjun.vxmq.event.mqtt.MqttProtocolErrorEvent;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.core.Vertx;
import io.vertx.mutiny.mqtt.MqttEndpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.function.Consumer;

/**
 * This will be called when an error at protocol level happens
 */
public class MqttExceptionHandler implements Consumer<Throwable> {

  private final static Logger LOGGER = LoggerFactory.getLogger(MqttExceptionHandler.class);

  private final MqttEndpoint mqttEndpoint;
  private final Vertx vertx;
  private final EventService eventService;

  public MqttExceptionHandler(MqttEndpoint mqttEndpoint, Vertx vertx, EventService eventService) {
    this.mqttEndpoint = mqttEndpoint;
    this.vertx = vertx;
    this.eventService = eventService;
  }

  @Override
  public void accept(Throwable throwable) {
    LOGGER.error("Error occurred at protocol level of " + mqttEndpoint.clientIdentifier(), throwable);
    Uni.createFrom().voidItem()
      .onItem().transformToUni(v -> publishMqttProtocolErrorEvent(mqttEndpoint, throwable))
      .subscribe().with(v -> {}, t -> LOGGER.error("Error occurred when processing mqtt exception", t));
  }

  private Uni<Void> publishMqttProtocolErrorEvent(MqttEndpoint mqttEndpoint, Throwable t) {
    Event event = new MqttProtocolErrorEvent(Instant.now().toEpochMilli(), VertxUtil.getNodeId(vertx),
      mqttEndpoint.clientIdentifier(), t.getMessage());
    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("Publishing event: {}, ", event.toJson());
    }
    return eventService.publishEvent(event);
  }

}
