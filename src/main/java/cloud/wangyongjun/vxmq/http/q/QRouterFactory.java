package cloud.wangyongjun.vxmq.http.q;

import cloud.wangyongjun.vxmq.assist.ConsumerUtil;
import cloud.wangyongjun.vxmq.http.api.ApiConstants;
import io.smallrye.mutiny.Uni;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.mutiny.core.MultiMap;
import io.vertx.mutiny.core.Vertx;
import io.vertx.mutiny.core.buffer.Buffer;
import io.vertx.mutiny.ext.web.Router;
import io.vertx.mutiny.ext.web.RoutingContext;

import java.util.Map;

public class QRouterFactory {

  public static Router router(Vertx vertx) {
    Router qRouter = Router.router(vertx);

    qRouter.route(ApiConstants.Q_PREFIX_PING).handler(QRouterFactory::pingHandler);

    qRouter.get(ApiConstants.Q_PREFIX_HEALTH).handler(HealthCheckFactory.healthCheckHandler(vertx));
    qRouter.get(ApiConstants.Q_PREFIX_HEALTH + "/started").handler(HealthCheckFactory.startedHealthCheckHandler(vertx));
    qRouter.get(ApiConstants.Q_PREFIX_HEALTH + "/live").handler(HealthCheckFactory.liveHealthCheckHandler(vertx));
    qRouter.get(ApiConstants.Q_PREFIX_HEALTH + "/ready").handler(HealthCheckFactory.readyHealthCheckHandler(vertx));

    return qRouter;
  }

  private static void pingHandler(RoutingContext routingContext) {
    JsonObject result = new JsonObject();

    result.put("path", routingContext.request().path());
    result.put("method", routingContext.request().method().name());
    result.put("headers", multiMapToJsonArray(routingContext.request().headers()));

    Uni.createFrom().voidItem()
      .onItem().transformToUni(v -> routingContext.response().send(Buffer.newInstance(result.toBuffer())))
      .subscribe().with(ConsumerUtil.nothingToDo(), Throwable::printStackTrace);
  }

  private static JsonArray multiMapToJsonArray(MultiMap multiMap) {
    JsonArray jsonArray = new JsonArray();
    for (Map.Entry<String, String> entry : multiMap) {
      JsonObject jsonObject = new JsonObject();
      jsonObject.put("name", entry.getKey());
      jsonObject.put("value", entry.getValue());
      jsonArray.add(jsonObject);
    }
    return jsonArray;
  }

}
