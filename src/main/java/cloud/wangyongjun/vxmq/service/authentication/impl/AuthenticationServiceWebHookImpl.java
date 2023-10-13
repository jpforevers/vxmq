package cloud.wangyongjun.vxmq.service.authentication.impl;

import cloud.wangyongjun.vxmq.assist.Config;
import cloud.wangyongjun.vxmq.service.authentication.AuthenticationService;
import cloud.wangyongjun.vxmq.service.authentication.MqttAuthData;
import cloud.wangyongjun.vxmq.service.authentication.MqttAuthResult;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.WebClientOptions;
import io.vertx.mutiny.core.Vertx;

public class AuthenticationServiceWebHookImpl implements AuthenticationService {

  private static volatile AuthenticationService authenticationService;

  public static AuthenticationService getInstance(Vertx vertx, JsonObject config) {
    if (authenticationService == null) {
      synchronized (AuthenticationServiceWebHookImpl.class) {
        if (authenticationService == null) {
          authenticationService = new AuthenticationServiceWebHookImpl(vertx, config);
        }
      }
    }
    return authenticationService;
  }

  private final String webhookUrl;
  private final WebClient webClient;

  private AuthenticationServiceWebHookImpl(Vertx vertx, JsonObject config) {
    this.webhookUrl = Config.getMqttAuthWebhookUrl(config);
    this.webClient = WebClient.create(vertx.getDelegate(), new WebClientOptions());
  }

  @Override
  public Future<MqttAuthResult> authenticate(MqttAuthData mqttAuthData) {

    return webClient.postAbs(webhookUrl).sendJsonObject(mqttAuthData.toJson())
      .map(bufferHttpResponse -> new MqttAuthResult(bufferHttpResponse.bodyAsJsonObject()));
  }

}
