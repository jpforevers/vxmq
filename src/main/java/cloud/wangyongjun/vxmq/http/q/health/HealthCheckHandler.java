package cloud.wangyongjun.vxmq.http.q.health;

import cloud.wangyongjun.vxmq.assist.Config;
import cloud.wangyongjun.vxmq.http.AbstractHandler;
import cloud.wangyongjun.vxmq.http.api.ApiConstants;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.smallrye.mutiny.Uni;
import io.vertx.ext.healthchecks.Status;
import io.vertx.ext.web.client.WebClientOptions;
import io.vertx.mutiny.core.Vertx;
import io.vertx.mutiny.ext.healthchecks.HealthChecks;
import io.vertx.mutiny.ext.web.RoutingContext;
import io.vertx.mutiny.ext.web.client.WebClient;

public class HealthCheckHandler extends AbstractHandler {

  protected static final String HEALTH_CHECK_NAME_HTTP = "http-server";

  public HealthCheckHandler(Vertx vertx) {
    super(vertx);
  }

  @Override
  public Uni<Void> handleRequest(RoutingContext routingContext) {
    HealthChecks hc = HealthChecks.create(vertx);
    hc.register(HEALTH_CHECK_NAME_HTTP, 3000, pingChecker(vertx));
    io.vertx.mutiny.ext.healthchecks.HealthCheckHandler healthCheckHandler = io.vertx.mutiny.ext.healthchecks.HealthCheckHandler.createWithHealthChecks(hc);
    healthCheckHandler.handle(routingContext);
    return Uni.createFrom().voidItem();
  }

  protected static Uni<Status> pingChecker(Vertx vertx) {
    return Uni.createFrom().voidItem()
      .onItem().transformToUni(v -> {
        WebClient webClient = WebClient.create(vertx, new WebClientOptions());
        return webClient.get(Config.getHttpServerPort(), "127.0.0.1", ApiConstants.Q_URL_PREFIX + ApiConstants.Q_PREFIX_PING).send()
          .onItem().transform(bufferHttpResponse -> bufferHttpResponse.statusCode() == HttpResponseStatus.OK.code() ? Status.OK() : Status.KO())
          .eventually(webClient::close);
      });
  }

}
