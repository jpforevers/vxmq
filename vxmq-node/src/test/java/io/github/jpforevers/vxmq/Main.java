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

import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.mutiny.core.Vertx;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main extends AbstractVerticle {

  private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);

  private static int count = 0;

  @Override
  public Uni<Void> asyncStart() {
    LOGGER.info("Starting...");
    return vertx.createHttpServer()
      .requestHandler(req -> {
        count++;
        LOGGER.info("count: " + count);
        req.response()
          .putHeader("content-type", "text/plain")
          .end("Hello from Vert.x!")
          .subscribe().with(v -> {}, Throwable::printStackTrace);
      })
      .listen(8080)
      .replaceWithVoid();
  }

  public static void main(String[] args) {
    Vertx vertx = Vertx.vertx();
    vertx.deployVerticle(Main.class.getName(), new DeploymentOptions().setInstances(2))
      .subscribe().with(v -> {}, Throwable::printStackTrace);
  }

}
