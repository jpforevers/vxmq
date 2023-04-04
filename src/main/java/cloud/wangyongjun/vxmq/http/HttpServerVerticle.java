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
import cloud.wangyongjun.vxmq.http.api.ApiFailureHandler;
import cloud.wangyongjun.vxmq.http.api.test.TestHandler;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.vertx.core.AbstractVerticle;
import io.vertx.core.http.HttpMethod;
import io.vertx.ext.web.AllowForwardHeaders;
import io.vertx.mutiny.ext.web.Router;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpServerVerticle extends AbstractVerticle {

  private static final Logger LOGGER = LoggerFactory.getLogger(HttpServerVerticle.class);

  @Override
  public Uni<Void> asyncStart() {
    Router rootRouter = Router.router(vertx);
    rootRouter.allowForward(AllowForwardHeaders.ALL);

    Router apiRouter = Router.router(vertx);
    registerApiRoute(apiRouter);

    rootRouter.route(ApiConstants.API_URL_PREFIX_V1 + "/*")
      .failureHandler(new ApiFailureHandler())
      .subRouter(apiRouter);

    return vertx.createHttpServer().requestHandler(rootRouter)
      .listen(Config.getHttpServerPort(config()))
      .replaceWithVoid();
  }

  @Override
  public Uni<Void> asyncStop() {
    return Uni.createFrom().voidItem();
  }

  /**
   * Register api route
   *
   * @param apiRouter api {@link io.vertx.ext.web.Router}
   */
  private void registerApiRoute(Router apiRouter) {
    apiRouter.route().method(HttpMethod.GET).path("/test").handler(new TestHandler(vertx));
  }

}
