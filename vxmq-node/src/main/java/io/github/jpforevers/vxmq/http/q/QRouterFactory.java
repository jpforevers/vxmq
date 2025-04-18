package io.github.jpforevers.vxmq.http.q;

import io.github.jpforevers.vxmq.assist.Config;
import io.github.jpforevers.vxmq.http.api.ApiConstants;
import io.github.jpforevers.vxmq.http.q.health.HealthCheckHandler;
import io.github.jpforevers.vxmq.http.q.health.LiveHealthCheckHandler;
import io.github.jpforevers.vxmq.http.q.health.ReadyHealthCheckHandler;
import io.github.jpforevers.vxmq.http.q.health.StartedHealthCheckHandler;
import io.github.jpforevers.vxmq.http.q.ping.PingHandler;
import io.vertx.mutiny.core.Vertx;
import io.vertx.mutiny.ext.web.Router;
import io.vertx.mutiny.micrometer.PrometheusScrapingHandler;

public class QRouterFactory {

  public static Router router(Vertx vertx) {
    Router qRouter = Router.router(vertx);

    qRouter.route(ApiConstants.Q_PREFIX_PING).handler(new PingHandler(vertx));

    qRouter.get(ApiConstants.Q_PREFIX_HEALTH).handler(new HealthCheckHandler(vertx));
    qRouter.get(ApiConstants.Q_PREFIX_HEALTH + "/started").handler(new StartedHealthCheckHandler(vertx));
    qRouter.get(ApiConstants.Q_PREFIX_HEALTH + "/live").handler(new LiveHealthCheckHandler(vertx));
    qRouter.get(ApiConstants.Q_PREFIX_HEALTH + "/ready").handler(new ReadyHealthCheckHandler(vertx));
    if (Config.getMetricsEnable()) {
      qRouter.route(ApiConstants.Q_PREFIX_METRICS).handler(routingContext -> PrometheusScrapingHandler.create().handle(routingContext));
    }

    return qRouter;
  }

}
