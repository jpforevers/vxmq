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

package io.github.jpforevers.vxmq.http;

import io.github.jpforevers.vxmq.assist.Config;
import io.github.jpforevers.vxmq.http.api.ApiConstants;
import io.github.jpforevers.vxmq.http.api.ApiFailureHandler;
import io.github.jpforevers.vxmq.http.api.ApiRouterFactory;
import io.github.jpforevers.vxmq.http.q.QRouterFactory;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.vertx.core.AbstractVerticle;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.ext.web.AllowForwardHeaders;
import io.vertx.mutiny.ext.web.Router;
import io.vertx.mutiny.ext.web.handler.ResponseTimeHandler;
import io.vertx.mutiny.ext.web.handler.StaticHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpServerVerticle extends AbstractVerticle {

  private final static Logger LOGGER = LoggerFactory.getLogger(HttpServerVerticle.class);

  @Override
  public Uni<Void> asyncStart() {
    Router rootRouter = Router.router(vertx);
    rootRouter.allowForward(AllowForwardHeaders.ALL);
    rootRouter.route().handler(ResponseTimeHandler.create());

    rootRouter.route(ApiConstants.Q_URL_PREFIX + "/*")
      .subRouter(QRouterFactory.router(vertx));

    rootRouter.route(ApiConstants.API_URL_PREFIX_V1 + "/*")
      .failureHandler(new ApiFailureHandler())
      .subRouter(ApiRouterFactory.router(vertx));

    // 处理前端路由（SPA），仅在请求的资源不存在时，才重定向到 index.html
    rootRouter.get().handler(ctx -> {
        if (ctx.normalizedPath().contains(".")) {
            // 说明是静态资源（例如 .js, .css, .png, .jpg 等），放行
            ctx.next();
        } else {
            // 说明是前端路由（如 /about, /dashboard），重定向到 index.html
            ctx.reroute("/index.html");;
        }
    });

    // 处理静态资源
    StaticHandler staticHandler = StaticHandler.create()
      .setCachingEnabled(true) // 可选：启用缓存
      .setDefaultContentEncoding("utf-8"); // 可选：防止乱码

    rootRouter.get().handler(staticHandler);

    HttpServerOptions httpServerOptions = new HttpServerOptions();
    httpServerOptions.setLogActivity(Config.getHttpServerLogActivity());
    return vertx.createHttpServer(httpServerOptions)
      .requestHandler(rootRouter)
      .exceptionHandler(t -> LOGGER.error("Error occurred at http server layer", t))
      .invalidRequestHandler(httpServerRequest -> LOGGER.error("Invalid request, method: {}, uri: {}", httpServerRequest.method(), httpServerRequest.uri()))
      .listen(Config.getHttpServerPort())
      .replaceWithVoid();
  }

  @Override
  public Uni<Void> asyncStop() {
    return Uni.createFrom().voidItem();
  }

}
