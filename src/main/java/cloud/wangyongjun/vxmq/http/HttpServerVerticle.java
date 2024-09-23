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
