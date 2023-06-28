/*
 * Copyright 2018-present 王用军
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package cloud.wangyongjun.vxmq.http.api;

import cloud.wangyongjun.vxmq.assist.ModelConstants;
import cloud.wangyongjun.vxmq.assist.ServiceFactory;
import cloud.wangyongjun.vxmq.http.api.ping.PingHandler;
import cloud.wangyongjun.vxmq.http.api.session.DeleteSessionByClientIdHandler;
import cloud.wangyongjun.vxmq.http.api.test.TestHandler;
import io.vertx.core.json.JsonObject;
import io.vertx.mutiny.core.Vertx;
import io.vertx.mutiny.ext.web.Router;

public class ApiRouterFactory {

  public static Router router(Vertx vertx, JsonObject config) {
    Router apiRouter = Router.router(vertx);

    apiRouter.route(ApiConstants.API_PREFIX_TEST + "/*").subRouter(testRouter(vertx));
    apiRouter.route(ApiConstants.API_PREFIX_PING + "/*").subRouter(pingRouter(vertx));
    apiRouter.route(ApiConstants.API_PREFIX_SESSION + "/*").subRouter(sessionRouter(vertx));
    return apiRouter;
  }

  private static Router testRouter(Vertx vertx) {
    Router router = Router.router(vertx);
    router.get().handler(new TestHandler(vertx));
    return router;
  }

  private static Router pingRouter(Vertx vertx) {
    Router router = Router.router(vertx);
    router.get().handler(new PingHandler(vertx));
    return router;
  }

  private static Router sessionRouter(Vertx vertx) {
    Router router = Router.router(vertx);
    router
      .delete("/:" + ModelConstants.FIELD_NAME_CLIENT_ID)
      .handler(new DeleteSessionByClientIdHandler(vertx, ServiceFactory.sessionService(vertx), ServiceFactory.clientService(vertx)));
    return router;
  }

}
