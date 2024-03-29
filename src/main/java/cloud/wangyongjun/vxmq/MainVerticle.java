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

package cloud.wangyongjun.vxmq;

import cloud.wangyongjun.vxmq.assist.Config;
import cloud.wangyongjun.vxmq.http.HttpServerVerticle;
import cloud.wangyongjun.vxmq.mqtt.MqttServerVerticle;
import cloud.wangyongjun.vxmq.mqtt.SessionCheckerVerticle;
import cloud.wangyongjun.vxmq.service.authentication.AuthenticationVerticle;
import cloud.wangyongjun.vxmq.service.sub.SubVerticle;
import cloud.wangyongjun.vxmq.rule.RuleVerticle;
import cloud.wangyongjun.vxmq.shell.ShellServerVerticle;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.http.impl.HttpServerImpl;
import io.vertx.core.impl.VertxInternal;
import io.vertx.core.net.impl.NetServerImpl;
import io.vertx.core.net.impl.ServerID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.Map;

public class MainVerticle extends AbstractVerticle {

  private static final Logger LOGGER = LoggerFactory.getLogger(MainVerticle.class);

  @Override
  public Uni<Void> asyncStart() {

    return Uni.createFrom().voidItem()
      .onItem().transformToUni(v -> showBanner())
      .onItem().transformToUni(v -> deployVerticle())
      .onItem().transformToUni(v -> printServers())
      .replaceWithVoid();
  }

  private Uni<Void> showBanner() {
    return vertx.fileSystem().readFile("banner.txt")
      .onItem().invoke(buffer -> LOGGER.info(buffer.toString(StandardCharsets.UTF_8)))
      .replaceWithVoid();
  }

  /**
   * Deploy application verticles
   *
   * @return void
   */
  private Uni<Void> deployVerticle() {

    return Uni.createFrom().voidItem()
      .onItem().transformToUni(s -> vertx.deployVerticle(HttpServerVerticle::new, new DeploymentOptions().setConfig(config()).setInstances(Config.AVAILABLE_CPU_CORE_SENSORS)))
      .onItem().invoke(s -> LOGGER.info("{} deployed", HttpServerVerticle.class.getSimpleName()))

      .onItem().transformToUni(s -> vertx.deployVerticle(MqttServerVerticle::new, new DeploymentOptions().setConfig(config()).setInstances(Config.AVAILABLE_CPU_CORE_SENSORS)))
      .onItem().invoke(s -> LOGGER.info("{} deployed", MqttServerVerticle.class.getSimpleName()))

      .onItem().transformToUni(s -> vertx.deployVerticle(SessionCheckerVerticle::new, new DeploymentOptions().setConfig(config()).setWorker(true)))
      .onItem().invoke(s -> LOGGER.info("{} deployed", SessionCheckerVerticle.class.getSimpleName()))

      .onItem().transformToUni(s -> vertx.deployVerticle(ShellServerVerticle::new, new DeploymentOptions().setConfig(config())))
      .onItem().invoke(s -> LOGGER.info("{} deployed", ShellServerVerticle.class.getSimpleName()))

      .onItem().transformToUni(s -> vertx.deployVerticle(RuleVerticle::new, new DeploymentOptions().setConfig(config())))
      .onItem().invoke(s -> LOGGER.info("{} deployed", RuleVerticle.class.getSimpleName()))

      .onItem().transformToUni(s -> vertx.deployVerticle(SubVerticle::new, new DeploymentOptions().setConfig(config())))
      .onItem().invoke(s -> LOGGER.info("{} deployed", SubVerticle.class.getSimpleName()))

      .onItem().transformToUni(s -> vertx.deployVerticle(AuthenticationVerticle::new, new DeploymentOptions().setConfig(config())))
      .onItem().invoke(s -> LOGGER.info("{} deployed", AuthenticationVerticle.class.getSimpleName()))

      .replaceWithVoid();
  }

  /**
   * Print application servers
   *
   * @return void
   */
  private Uni<Void> printServers() {
    VertxInternal vertxInternal = (VertxInternal) vertx.getDelegate();
    LOGGER.info("Net Servers:");
    for (Map.Entry<ServerID, NetServerImpl> server : vertxInternal.sharedNetServers().entrySet()) {
      LOGGER.info(server.getKey().host + ":" + server.getKey().port);
    }
    LOGGER.info("HTTP Servers:");
    for (Map.Entry<ServerID, HttpServerImpl> server : vertxInternal.sharedHttpServers().entrySet()) {
      LOGGER.info(server.getKey().host + ":" + server.getKey().port);
    }
    return Uni.createFrom().voidItem();
  }

}
