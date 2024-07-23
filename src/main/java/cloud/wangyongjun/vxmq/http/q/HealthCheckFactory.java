package cloud.wangyongjun.vxmq.http.q;

import cloud.wangyongjun.vxmq.assist.Config;
import cloud.wangyongjun.vxmq.http.api.ApiConstants;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.smallrye.mutiny.Uni;
import io.vertx.ext.healthchecks.Status;
import io.vertx.ext.web.client.WebClientOptions;
import io.vertx.mutiny.core.Vertx;
import io.vertx.mutiny.ext.healthchecks.HealthCheckHandler;
import io.vertx.mutiny.ext.healthchecks.HealthChecks;
import io.vertx.mutiny.ext.web.client.WebClient;

public class HealthCheckFactory {

  private static final String HEALTH_CHECK_NAME_HTTP = "http-server";

  public static HealthCheckHandler healthCheckHandler(Vertx vertx) {
    return HealthCheckHandler.createWithHealthChecks(configHealthChecks(vertx));
  }

  public static HealthCheckHandler startedHealthCheckHandler(Vertx vertx) {
    return HealthCheckHandler.createWithHealthChecks(configStartedHealthChecks(vertx));
  }

  public static HealthCheckHandler liveHealthCheckHandler(Vertx vertx) {
    return HealthCheckHandler.createWithHealthChecks(configLiveHealthChecks(vertx));
  }

  public static HealthCheckHandler readyHealthCheckHandler(Vertx vertx) {
    return HealthCheckHandler.createWithHealthChecks(configReadyHealthChecks(vertx));
  }

  private static HealthChecks configHealthChecks(Vertx vertx) {
    HealthChecks hc = HealthChecks.create(vertx);
    hc.register(HEALTH_CHECK_NAME_HTTP, 3000, checkHttpServer(vertx));
    return hc;
  }

  private static HealthChecks configStartedHealthChecks(Vertx vertx) {
    HealthChecks hc = HealthChecks.create(vertx);
    hc.register(HEALTH_CHECK_NAME_HTTP, 3000, checkHttpServer(vertx));
    return hc;
  }

  private static HealthChecks configLiveHealthChecks(Vertx vertx) {
    HealthChecks hc = HealthChecks.create(vertx);
    hc.register(HEALTH_CHECK_NAME_HTTP, 3000, checkHttpServer(vertx));
    return hc;
  }

  private static HealthChecks configReadyHealthChecks(Vertx vertx) {
    HealthChecks hc = HealthChecks.create(vertx);
    hc.register(HEALTH_CHECK_NAME_HTTP, 3000, checkHttpServer(vertx));
    return hc;
  }

  private static Uni<Status> checkHttpServer(Vertx vertx) {
    return Uni.createFrom().voidItem()
      .onItem().transformToUni(v -> {
        WebClient webClient = WebClient.create(vertx, new WebClientOptions());
        return webClient.get(Config.getHttpServerPort(), "127.0.0.1", ApiConstants.Q_URL_PREFIX + ApiConstants.Q_PREFIX_PING).send()
          .onItem().transform(bufferHttpResponse -> bufferHttpResponse.statusCode() == HttpResponseStatus.OK.code() ? Status.OK() : Status.KO())
          .eventually(webClient::close);
      });
  }

}
