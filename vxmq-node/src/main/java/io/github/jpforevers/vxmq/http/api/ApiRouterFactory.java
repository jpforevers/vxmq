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
import io.github.jpforevers.vxmq.http.api.session.GetAllSessionsHandler;
import io.github.jpforevers.vxmq.service.ServiceFactory;
import io.github.jpforevers.vxmq.http.api.session.DeleteSessionByClientIdHandler;
import io.github.jpforevers.vxmq.http.api.test.TestHandler;
import io.vertx.mutiny.core.Vertx;
import io.vertx.mutiny.ext.web.Router;

public class ApiRouterFactory {

  public static Router router(Vertx vertx) {
    Router apiRouter = Router.router(vertx);

    apiRouter.get(ApiConstants.API_PREFIX_TEST)
      .handler(new TestHandler(vertx));

    apiRouter.delete(ApiConstants.API_PREFIX_SESSION + "/:" + ModelConstants.FIELD_NAME_CLIENT_ID)
      .handler(new DeleteSessionByClientIdHandler(vertx, ServiceFactory.compositeService(vertx)));

    apiRouter.get(ApiConstants.API_PREFIX_SESSION)
      .handler(new GetAllSessionsHandler(vertx, ServiceFactory.sessionService(vertx)));

    return apiRouter;
  }

}
