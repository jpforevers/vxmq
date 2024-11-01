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

package io.github.jpforevers.vxmq.http.api.session;

import io.github.jpforevers.vxmq.assist.ModelConstants;
import io.github.jpforevers.vxmq.http.api.AbstractApiJsonResultHandler;
import io.github.jpforevers.vxmq.service.composite.CompositeService;
import io.smallrye.mutiny.Uni;
import io.vertx.core.json.JsonObject;
import io.vertx.mutiny.core.Vertx;
import io.vertx.mutiny.ext.web.RoutingContext;

public class DeleteSessionByClientIdHandler extends AbstractApiJsonResultHandler {

  private final CompositeService compositeService;

  public DeleteSessionByClientIdHandler(Vertx vertx, CompositeService compositeService) {
    super(vertx);
    this.compositeService = compositeService;
  }

  @Override
  public Uni<Object> computeJsonResult(RoutingContext routingContext) {
    String clientId = routingContext.pathParam(ModelConstants.FIELD_NAME_CLIENT_ID);
    return compositeService.deleteSession(clientId).replaceWith(new JsonObject());
  }

}
