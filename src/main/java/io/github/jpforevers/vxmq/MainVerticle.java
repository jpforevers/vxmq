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

package io.github.jpforevers.vxmq;

import io.github.jpforevers.vxmq.assist.Config;
import io.github.jpforevers.vxmq.assist.VertxUtil;
import io.github.jpforevers.vxmq.http.HttpServerVerticle;
import io.github.jpforevers.vxmq.mqtt.DirtyClientVerticleCleaner;
import io.github.jpforevers.vxmq.mqtt.MqttServerVerticle;
import io.github.jpforevers.vxmq.service.authentication.AuthenticationVerticle;
import io.github.jpforevers.vxmq.service.sub.SubVerticle;
import io.github.jpforevers.vxmq.rule.RuleVerticle;
import io.github.jpforevers.vxmq.shell.ShellServerVerticle;
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

      .onItem().transformToUni(s -> vertx.deployVerticle(RuleVerticle::new, new DeploymentOptions()))
      .onItem().invoke(s -> LOGGER.info("{} deployed", RuleVerticle.class.getSimpleName()))

      .onItem().transformToUni(s -> vertx.deployVerticle(SubVerticle::new, new DeploymentOptions()))
      .onItem().invoke(s -> LOGGER.info("{} deployed", SubVerticle.class.getSimpleName()))

      .onItem().transformToUni(s -> vertx.deployVerticle(AuthenticationVerticle::new, new DeploymentOptions()))
      .onItem().invoke(s -> LOGGER.info("{} deployed", AuthenticationVerticle.class.getSimpleName()))

      .onItem().transformToUni(s -> vertx.deployVerticle(DirtyClientVerticleCleaner::new, new DeploymentOptions()))
      .onItem().invoke(s -> LOGGER.info("{} deployed", DirtyClientVerticleCleaner.class.getSimpleName()))

      .onItem().transformToUni(s -> vertx.deployVerticle(ShellServerVerticle::new, new DeploymentOptions()))
      .onItem().invoke(s -> LOGGER.info("{} deployed", ShellServerVerticle.class.getSimpleName()))

      .onItem().transformToUni(s -> vertx.deployVerticle(HttpServerVerticle::new, new DeploymentOptions().setInstances(Config.AVAILABLE_CPU_CORE_SENSORS)))
      .onItem().invoke(s -> LOGGER.info("{} deployed", HttpServerVerticle.class.getSimpleName()))

      .onItem().transformToUni(s -> vertx.deployVerticle(MqttServerVerticle::new, new DeploymentOptions().setInstances(Config.AVAILABLE_CPU_CORE_SENSORS)))
      .onItem().invoke(s -> LOGGER.info("{} deployed", MqttServerVerticle.class.getSimpleName()))

      .replaceWithVoid();
  }

  /**
   * Print application servers
   *
   * @return void
   */
  private Uni<Void> printServers() {
    VertxInternal vertxInternal = VertxUtil.getVertxInternal(vertx);
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
