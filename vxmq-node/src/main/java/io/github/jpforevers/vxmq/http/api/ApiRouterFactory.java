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
import io.github.jpforevers.vxmq.http.api.v2.ApiV2AuthHandler;
import io.github.jpforevers.vxmq.http.api.v2.session.GetSessionsHandler;
import io.vertx.mutiny.core.Vertx;
import io.vertx.mutiny.ext.web.Router;
import io.vertx.mutiny.ext.web.handler.BodyHandler;

public class ApiRouterFactory {

  public static Router apiRouter(Vertx vertx) {
    Router apiRouter = Router.router(vertx);

    apiRouter.route()
      .handler(BodyHandler.create()
        .setBodyLimit(10 * 1024 * 1024)
      )
      .handler(new ApiV2AuthHandler(vertx));

    apiRouter.route(ApiConstants.API_URL_PREFIX_VERSION_V1 + "/*")
      .subRouter(apiV1Router(vertx));
      
    apiRouter.route(ApiConstants.API_URL_PREFIX_VERSION_V2 + "/*")
      .subRouter(apiV2Router(vertx));

    apiRouter.route()
      .handler(routingContext -> {
        if (!routingContext.response().ended()) {
          routingContext.fail(new ApiException(ApiErrorCode.COMMON_NOT_FOUND));
        } else {
          routingContext.next();
        }
      });      
      
    return apiRouter;  
  }

  private static Router apiV1Router(Vertx vertx) {
    Router apiV1Router = Router.router(vertx);

    apiV1Router.get(ApiConstants.API_PREFIX_TEST)
      .handler(new TestHandler(vertx));

    apiV1Router.delete(ApiConstants.API_PREFIX_SESSION + "/:" + ModelConstants.FIELD_NAME_CLIENT_ID)
      .handler(new DeleteSessionByClientIdHandler(vertx, ServiceFactory.compositeService(vertx)));

    apiV1Router.get(ApiConstants.API_PREFIX_SESSION)
      .handler(new GetAllSessionsHandler(vertx, ServiceFactory.sessionService(vertx)));

    return apiV1Router;
  }

  private static Router apiV2Router(Vertx vertx) {
    Router apiV2Router = Router.router(vertx);
    apiV2Router.route().handler(new ApiV2AuthHandler(vertx));

    apiV2Router.route(ApiConstants.API_PREFIX_SESSIONS)
      .handler(GetSessionsHandler.validationHandler(vertx))
      .handler(new GetSessionsHandler(vertx, ServiceFactory.sessionService(vertx)));

    return apiV2Router;
  }

}
