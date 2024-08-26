package cloud.wangyongjun.vxmq.http.q;

import cloud.wangyongjun.vxmq.http.api.ApiConstants;
import cloud.wangyongjun.vxmq.http.q.health.HealthCheckHandler;
import cloud.wangyongjun.vxmq.http.q.health.LiveHealthCheckHandler;
import cloud.wangyongjun.vxmq.http.q.health.ReadyHealthCheckHandler;
import cloud.wangyongjun.vxmq.http.q.health.StartedHealthCheckHandler;
import cloud.wangyongjun.vxmq.http.q.ping.PingHandler;
import io.vertx.mutiny.core.Vertx;
import io.vertx.mutiny.ext.web.Router;

public class QRouterFactory {

  public static Router router(Vertx vertx) {
    Router qRouter = Router.router(vertx);

    qRouter.route(ApiConstants.Q_PREFIX_PING).handler(new PingHandler(vertx));

    qRouter.get(ApiConstants.Q_PREFIX_HEALTH).handler(new HealthCheckHandler(vertx));
    qRouter.get(ApiConstants.Q_PREFIX_HEALTH + "/started").handler(new StartedHealthCheckHandler(vertx));
    qRouter.get(ApiConstants.Q_PREFIX_HEALTH + "/live").handler(new LiveHealthCheckHandler(vertx));
    qRouter.get(ApiConstants.Q_PREFIX_HEALTH + "/ready").handler(new ReadyHealthCheckHandler(vertx));

    return qRouter;
  }

}
