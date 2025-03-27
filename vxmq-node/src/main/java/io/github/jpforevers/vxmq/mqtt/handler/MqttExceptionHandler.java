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

import io.github.jpforevers.vxmq.assist.VertxUtil;
import io.github.jpforevers.vxmq.event.Event;
import io.github.jpforevers.vxmq.event.EventService;
import io.github.jpforevers.vxmq.event.mqtt.MqttProtocolErrorEvent;
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
