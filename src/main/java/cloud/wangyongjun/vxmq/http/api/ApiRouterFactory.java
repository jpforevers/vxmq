/*
 * Copyright 2018-present 王用军
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package cloud.wangyongjun.vxmq.http.api;

import cloud.wangyongjun.vxmq.assist.Config;
import cloud.wangyongjun.vxmq.http.api.ping.PingHandler;
import cloud.wangyongjun.vxmq.http.api.test.TestHandler;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.mqtt.MqttConnectReturnCode;
import io.smallrye.mutiny.Uni;
import io.vertx.core.http.HttpMethod;
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

public class ApiRouterFactory {

  public static Router router(Vertx vertx, JsonObject config) {
    Router apiRouter = Router.router(vertx);
    apiRouter.route().method(HttpMethod.GET).path(ApiConstants.API_PREFIX_HEALTH).handler(HealthCheckHandler.createWithHealthChecks(configHealthChecks(vertx, config)));

    apiRouter.route(ApiConstants.API_PREFIX_TEST + "/*").subRouter(testRouter(vertx));
    apiRouter.route(ApiConstants.API_PREFIX_PING + "/*").subRouter(pingRouter(vertx));
    return apiRouter;
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

  private static Router testRouter(Vertx vertx) {
    Router router = Router.router(vertx);
    router.get().handler(new TestHandler(vertx));
    return router;
  }

  private static Router pingRouter(Vertx vertx) {
    Router router = Router.router(vertx);
    router.get().handler(new PingHandler(vertx));
    return router;
  }

}
