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
import cloud.wangyongjun.vxmq.assist.ServiceFactory;
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
    MqttServerOptions mqttServerOptions = new MqttServerOptions().setAutoClientId(false).setPort(Config.getMqttServerPort(config()));
    MqttServer mqttServer = MqttServer.create(vertx, mqttServerOptions);
    mqttServer.endpointHandler(new MqttEndpointHandler(vertx, config(),
      ServiceFactory.sessionService(vertx, config()),
      ServiceFactory.msgService(vertx, config()),
      ServiceFactory.willService(vertx, config()),
      ServiceFactory.clientService(vertx),
      ServiceFactory.subService(vertx),
      ServiceFactory.retainService(vertx, config()),
      ServiceFactory.compositeService(vertx, config()),
      ServiceFactory.eventService(vertx))
    );
    mqttServer.exceptionHandler(t -> LOGGER.error("Error occurred at mqtt server layer", t));
    return mqttServer.listen().replaceWithVoid();
  }

  @Override
  public Uni<Void> asyncStop() {
    return Uni.createFrom().voidItem();
  }

}
