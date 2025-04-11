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

import io.github.jpforevers.vxmq.assist.ModelConstants;
import io.github.jpforevers.vxmq.http.api.v1.session.GetAllSessionsHandler;
import io.github.jpforevers.vxmq.service.ServiceFactory;
import io.github.jpforevers.vxmq.http.api.v1.session.DeleteSessionByClientIdHandler;
import io.github.jpforevers.vxmq.http.api.v1.test.TestHandler;
import io.github.jpforevers.vxmq.http.api.v2.AuthHandler;
import io.github.jpforevers.vxmq.http.api.v2.session.GetSessionsHandler;
import io.vertx.mutiny.core.Vertx;
import io.vertx.mutiny.ext.web.Router;

public class ApiRouterFactory {

  public static Router v1Router(Vertx vertx) {
    Router apiV1Router = Router.router(vertx);

    apiV1Router.get(ApiConstants.API_PREFIX_TEST)
      .handler(new TestHandler(vertx));

    apiV1Router.delete(ApiConstants.API_PREFIX_SESSION + "/:" + ModelConstants.FIELD_NAME_CLIENT_ID)
      .handler(new DeleteSessionByClientIdHandler(vertx, ServiceFactory.compositeService(vertx)));

    apiV1Router.get(ApiConstants.API_PREFIX_SESSION)
      .handler(new GetAllSessionsHandler(vertx, ServiceFactory.sessionService(vertx)));

    return apiV1Router;
  }

  public static Router v2Router(Vertx vertx) {
    Router apiV2Router = Router.router(vertx);
    apiV2Router.route().handler(new AuthHandler(vertx));
    apiV2Router.route(ApiConstants.API_PREFIX_SESSIONS).handler(new GetSessionsHandler(vertx, ServiceFactory.sessionService(vertx)));
    return apiV2Router;
  }

}
