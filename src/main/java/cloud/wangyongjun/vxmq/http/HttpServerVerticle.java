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

package cloud.wangyongjun.vxmq.http;

import cloud.wangyongjun.vxmq.assist.Config;
import cloud.wangyongjun.vxmq.http.api.ApiConstants;
import cloud.wangyongjun.vxmq.http.api.ApiFailureHandler;
import cloud.wangyongjun.vxmq.http.api.ApiRouterFactory;
import cloud.wangyongjun.vxmq.http.q.QRouterFactory;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.vertx.core.AbstractVerticle;
import io.vertx.ext.web.AllowForwardHeaders;
import io.vertx.mutiny.ext.web.Router;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpServerVerticle extends AbstractVerticle {

  private final static Logger LOGGER = LoggerFactory.getLogger(HttpServerVerticle.class);

  @Override
  public Uni<Void> asyncStart() {
    Router rootRouter = Router.router(vertx);
    rootRouter.allowForward(AllowForwardHeaders.ALL);

    rootRouter.route(ApiConstants.Q_URL_PREFIX + "/*")
      .subRouter(QRouterFactory.router(vertx));

    rootRouter.route(ApiConstants.API_URL_PREFIX_V1 + "/*")
      .failureHandler(new ApiFailureHandler())
      .subRouter(ApiRouterFactory.router(vertx));

    return vertx.createHttpServer().requestHandler(rootRouter)
      .exceptionHandler(t -> LOGGER.error("Error occurred at http server layer", t))
      .listen(Config.getHttpServerPort())
      .replaceWithVoid();
  }

  @Override
  public Uni<Void> asyncStop() {
    return Uni.createFrom().voidItem();
  }

}
