package cloud.wangyongjun.vxmq.http.q;

import cloud.wangyongjun.vxmq.assist.Config;
import cloud.wangyongjun.vxmq.http.api.ApiConstants;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.smallrye.mutiny.Uni;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.healthchecks.Status;
import io.vertx.ext.web.client.WebClientOptions;
import io.vertx.mutiny.core.Vertx;
import io.vertx.mutiny.ext.healthchecks.HealthCheckHandler;
import io.vertx.mutiny.ext.healthchecks.HealthChecks;
import io.vertx.mutiny.ext.web.client.WebClient;

public class HealthCheckFactory {

  private static final String HEALTH_CHECK_NAME_HTTP = "http-server";

  public static HealthCheckHandler healthCheckHandler(Vertx vertx, JsonObject config) {
    return HealthCheckHandler.createWithHealthChecks(configHealthChecks(vertx, config));
  }

  public static HealthCheckHandler startedHealthCheckHandler(Vertx vertx, JsonObject config) {
    return HealthCheckHandler.createWithHealthChecks(configStartedHealthChecks(vertx, config));
  }

  public static HealthCheckHandler liveHealthCheckHandler(Vertx vertx, JsonObject config) {
    return HealthCheckHandler.createWithHealthChecks(configLiveHealthChecks(vertx, config));
  }

  public static HealthCheckHandler readyHealthCheckHandler(Vertx vertx, JsonObject config) {
    return HealthCheckHandler.createWithHealthChecks(configReadyHealthChecks(vertx, config));
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
