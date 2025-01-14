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

package io.github.jpforevers.vxmq.mqtt;

import io.github.jpforevers.vxmq.assist.Config;
import io.github.jpforevers.vxmq.metrics.MetricsFactory;
import io.github.jpforevers.vxmq.service.ServiceFactory;
import io.github.jpforevers.vxmq.mqtt.handler.MqttEndpointHandler;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.vertx.core.AbstractVerticle;
import io.vertx.mqtt.MqttServerOptions;
import io.vertx.mutiny.mqtt.MqttServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MqttServerVerticle extends AbstractVerticle {

  private final static Logger LOGGER = LoggerFactory.getLogger(MqttServerVerticle.class);

  @Override
  public Uni<Void> asyncStart() {
    MqttServerOptions mqttServerOptions = new MqttServerOptions()
      .setMaxMessageSize(Config.getMqttMessageSizeMax())
      .setAutoClientId(false)
      .setPort(Config.getMqttServerPort())
      .setUseProxyProtocol(Config.getMqttProxyProtocolEnable());
    MqttServer mqttServer = MqttServer.create(vertx, mqttServerOptions);
    mqttServer.endpointHandler(new MqttEndpointHandler(vertx,
      ServiceFactory.sessionService(vertx),
      ServiceFactory.msgService(vertx),
      ServiceFactory.willService(vertx),
      ServiceFactory.clientService(vertx),
      ServiceFactory.subService(vertx),
      ServiceFactory.retainService(vertx),
      ServiceFactory.compositeService(vertx),
      ServiceFactory.eventService(vertx),
      ServiceFactory.authenticationService(vertx),
      ServiceFactory.inboundTopicAliasService(vertx),
      ServiceFactory.outboundTopicAliasService(vertx),
      MetricsFactory.getPacketsPublishReceivedCounter(),
      MetricsFactory.getPacketsPublishSentCounter(),
      Config.getFlowControlInboundReceiveMaximum(),
      ServiceFactory.flowService(vertx))
    );
    mqttServer.exceptionHandler(t -> LOGGER.error("Error occurred at mqtt server layer", t));
    return mqttServer.listen().replaceWithVoid();
  }

  @Override
  public Uni<Void> asyncStop() {
    return Uni.createFrom().voidItem();
  }

}
