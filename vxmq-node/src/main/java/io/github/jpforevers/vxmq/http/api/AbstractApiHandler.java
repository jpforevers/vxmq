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

package io.github.jpforevers.vxmq.http.api;

import io.github.jpforevers.vxmq.http.AbstractHandler;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.core.Vertx;
import io.vertx.mutiny.ext.web.RoutingContext;

public abstract class AbstractApiHandler extends AbstractHandler {

  protected AbstractApiHandler(Vertx vertx) {
    super(vertx);
  }

  @Override
  public Uni<Void> handleRequest(RoutingContext routingContext) {
    return handleApiRequest(routingContext);
  }

  public abstract Uni<Void> handleApiRequest(RoutingContext routingContext);

}
