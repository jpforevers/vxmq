package cloud.wangyongjun.vxmq.http.q;

import cloud.wangyongjun.vxmq.assist.Config;
import cloud.wangyongjun.vxmq.assist.ConsumerUtil;
import cloud.wangyongjun.vxmq.http.api.ApiConstants;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.smallrye.mutiny.Uni;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.healthchecks.Status;
import io.vertx.ext.web.client.WebClientOptions;
import io.vertx.mutiny.core.MultiMap;
import io.vertx.mutiny.core.Vertx;
import io.vertx.mutiny.core.buffer.Buffer;
import io.vertx.mutiny.ext.healthchecks.HealthCheckHandler;
import io.vertx.mutiny.ext.healthchecks.HealthChecks;
import io.vertx.mutiny.ext.web.Router;
import io.vertx.mutiny.ext.web.RoutingContext;
import io.vertx.mutiny.ext.web.client.WebClient;

import java.util.Map;

public class QRouterFactory {

  private static final String HEALTH_CHECK_NAME_HTTP = "http-server";

  public static Router router(Vertx vertx, JsonObject config) {
    Router qRouter = Router.router(vertx);

    qRouter.route(ApiConstants.Q_PREFIX_PING).handler(QRouterFactory::pingHandler);

    qRouter.get(ApiConstants.Q_PREFIX_HEALTH).handler(HealthCheckHandler.createWithHealthChecks(configHealthChecks(vertx, config)));
    qRouter.get(ApiConstants.Q_PREFIX_HEALTH + "/started").handler(HealthCheckHandler.createWithHealthChecks(configStartedHealthChecks(vertx, config)));
    qRouter.get(ApiConstants.Q_PREFIX_HEALTH + "/live").handler(HealthCheckHandler.createWithHealthChecks(configLiveHealthChecks(vertx, config)));
    qRouter.get(ApiConstants.Q_PREFIX_HEALTH + "/ready").handler(HealthCheckHandler.createWithHealthChecks(configReadyHealthChecks(vertx, config)));

    return qRouter;
  }

  private static void pingHandler(RoutingContext routingContext) {
    JsonObject result = new JsonObject();

    result.put("host", routingContext.request().host());
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

  private static HealthChecks configHealthChecks(Vertx vertx, JsonObject config) {
    HealthChecks hc = HealthChecks.create(vertx);
    hc.register(HEALTH_CHECK_NAME_HTTP, 3000, checkHttpServer(vertx, config));
    return hc;
  }

  private static HealthChecks configStartedHealthChecks(Vertx vertx, JsonObject config) {
    HealthChecks hc = HealthChecks.create(vertx);
    hc.register(HEALTH_CHECK_NAME_HTTP, 3000, checkHttpServer(vertx, config));
    return hc;
  }

  private static HealthChecks configLiveHealthChecks(Vertx vertx, JsonObject config) {
    HealthChecks hc = HealthChecks.create(vertx);
    hc.register(HEALTH_CHECK_NAME_HTTP, 3000, checkHttpServer(vertx, config));
    return hc;
  }

  private static HealthChecks configReadyHealthChecks(Vertx vertx, JsonObject config) {
    HealthChecks hc = HealthChecks.create(vertx);
    hc.register(HEALTH_CHECK_NAME_HTTP, 3000, checkHttpServer(vertx, config));
    return hc;
  }

  private static Uni<Status> checkHttpServer(Vertx vertx, JsonObject config) {
    return Uni.createFrom().voidItem()
      .onItem().transformToUni(v -> {
        WebClient webClient = WebClient.create(vertx, new WebClientOptions());
        return webClient.get(Config.getHttpServerPort(config), "127.0.0.1", ApiConstants.Q_URL_PREFIX + ApiConstants.Q_PREFIX_PING).send()
          .onItem().transform(bufferHttpResponse -> bufferHttpResponse.statusCode() == HttpResponseStatus.OK.code() ? Status.OK() : Status.KO())
          .eventually(webClient::close);
      });
  }

}
