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

package io.github.jpforevers.vxmq.http;

import io.github.jpforevers.vxmq.assist.ConsumerUtil;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.core.Vertx;
import io.vertx.mutiny.ext.web.RoutingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Consumer;

public abstract class AbstractHandler implements Consumer<RoutingContext> {

  protected final Logger logger = LoggerFactory.getLogger(this.getClass());

  protected final Vertx vertx;

  protected AbstractHandler(Vertx vertx) {
    this.vertx = vertx;
  }

  @Override
  public void accept(RoutingContext routingContext) {
    if (logger.isDebugEnabled()){
      logger.debug("{} request from {} to {}", routingContext.request().method(), routingContext.request().remoteAddress(), routingContext.request().uri());
    }
    handleRequest(routingContext).subscribe().with(ConsumerUtil.nothingToDo(), routingContext::fail);
  }

  public abstract Uni<Void> handleRequest(RoutingContext routingContext);

}
