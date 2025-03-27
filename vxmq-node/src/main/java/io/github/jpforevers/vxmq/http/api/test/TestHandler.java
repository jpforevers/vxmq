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

package io.github.jpforevers.vxmq.http.api.test;

import io.github.jpforevers.vxmq.http.api.AbstractApiJsonResultHandler;
import io.github.jpforevers.vxmq.http.api.ApiErrorCode;
import io.github.jpforevers.vxmq.http.api.ApiException;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.core.Vertx;
import io.vertx.mutiny.ext.web.RoutingContext;

public class TestHandler extends AbstractApiJsonResultHandler {

  public TestHandler(Vertx vertx) {
    super(vertx);
  }

  @Override
  public Uni<Object> computeJsonResult(RoutingContext routingContext) {
//    int x = 1 / 0;
//    return Uni.createFrom().item(new JsonObject().put("x", "哈哈哈"));
    throw new ApiException(ApiErrorCode.COMMON_FORBIDDEN, "没有权限");
  }

}
