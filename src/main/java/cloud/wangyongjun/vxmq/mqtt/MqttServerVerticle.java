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

package cloud.wangyongjun.vxmq.mqtt;

import cloud.wangyongjun.vxmq.assist.Config;
import cloud.wangyongjun.vxmq.metrics.MetricsFactory;
import cloud.wangyongjun.vxmq.service.ServiceFactory;
import cloud.wangyongjun.vxmq.mqtt.handler.MqttEndpointHandler;
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
      .setMaxMessageSize(10 * 1024 * 1024)
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
      MetricsFactory.getPacketsPublishReceivedCounter(),
      MetricsFactory.getPacketsPublishSentCounter())
    );
    mqttServer.exceptionHandler(t -> LOGGER.error("Error occurred at mqtt server layer", t));
    return mqttServer.listen().replaceWithVoid();
  }

  @Override
  public Uni<Void> asyncStop() {
    return Uni.createFrom().voidItem();
  }

}
