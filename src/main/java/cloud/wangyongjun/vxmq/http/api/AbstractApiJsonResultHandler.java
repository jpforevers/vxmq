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

package cloud.wangyongjun.vxmq.http.api;

import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.core.Vertx;
import io.vertx.mutiny.ext.web.RoutingContext;

public abstract class AbstractApiJsonResultHandler extends AbstractApiHandler {

  protected AbstractApiJsonResultHandler(Vertx vertx) {
    super(vertx);
  }

  @Override
  public Uni<Void> handleApiRequest(RoutingContext routingContext) {
    return computeJsonResult(routingContext)
      .onItem().transformToUni(o -> o == null ? routingContext.end() : routingContext.json(o));
  }

  public abstract Uni<Object> computeJsonResult(RoutingContext routingContext);

}
