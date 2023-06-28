package cloud.wangyongjun.vxmq.http.q;

import cloud.wangyongjun.vxmq.assist.Config;
import cloud.wangyongjun.vxmq.http.api.ApiConstants;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.mqtt.MqttConnectReturnCode;
import io.smallrye.mutiny.Uni;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.healthchecks.Status;
import io.vertx.ext.web.client.WebClientOptions;
import io.vertx.mqtt.MqttClientOptions;
import io.vertx.mutiny.core.Vertx;
import io.vertx.mutiny.ext.healthchecks.HealthCheckHandler;
import io.vertx.mutiny.ext.healthchecks.HealthChecks;
import io.vertx.mutiny.ext.web.Router;
import io.vertx.mutiny.ext.web.client.WebClient;
import io.vertx.mutiny.mqtt.MqttClient;

public class QRouterFactory {

  public static Router router(Vertx vertx, JsonObject config) {
    Router qRouter = Router.router(vertx);

    qRouter.route(ApiConstants.Q_PREFIX_HEALTH + "/*").subRouter(healthRouter(vertx, config));
    return qRouter;
  }

  private static Router healthRouter(Vertx vertx, JsonObject config) {
    Router router = Router.router(vertx);
    router.get().handler(HealthCheckHandler.createWithHealthChecks(configHealthChecks(vertx, config)));
    return router;
  }

  private static HealthChecks configHealthChecks(Vertx vertx, JsonObject config) {
    HealthChecks hc = HealthChecks.create(vertx);
    hc.register("mqtt-server", 3000, Uni.createFrom().voidItem()
      .onItem().transformToUni(v -> {
        MqttClient mqttClient = MqttClient.create(vertx, new MqttClientOptions().setClientId("health-check-client"));
        return mqttClient.connect(Config.getMqttServerPort(config), "127.0.0.1")
          .onItem().transform(mqttConnAckMessage -> mqttConnAckMessage.code().equals(MqttConnectReturnCode.CONNECTION_ACCEPTED) ? Status.OK() : Status.KO())
          .eventually(mqttClient::disconnectAndForget);
      }));
    hc.register("http-server", 3000, Uni.createFrom().voidItem()
      .onItem().transformToUni(v -> {
        WebClient webClient = WebClient.create(vertx, new WebClientOptions());
        return webClient.get(Config.getHttpServerPort(config), "127.0.0.1", ApiConstants.API_URL_PREFIX_V1 + ApiConstants.API_PREFIX_PING).send()
          .onItem().transform(bufferHttpResponse -> bufferHttpResponse.statusCode() == HttpResponseStatus.OK.code() ? Status.OK() : Status.KO())
          .eventually(webClient::close);
      }));
    return hc;
  }

}
