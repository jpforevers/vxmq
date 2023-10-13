package cloud.wangyongjun.vxmq.service.authentication.impl;

import cloud.wangyongjun.vxmq.service.authentication.AuthenticationService;
import cloud.wangyongjun.vxmq.service.authentication.MqttAuthData;
import cloud.wangyongjun.vxmq.service.authentication.MqttAuthResult;
import io.netty.handler.codec.mqtt.MqttConnectReturnCode;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.mutiny.core.Vertx;

public class AuthenticationServiceNoneImpl implements AuthenticationService {

  private static volatile AuthenticationService authenticationService;

  public static AuthenticationService getInstance(Vertx vertx, JsonObject config) {
    if (authenticationService == null) {
      synchronized (AuthenticationServiceNoneImpl.class) {
        if (authenticationService == null) {
          authenticationService = new AuthenticationServiceNoneImpl(vertx, config);
        }
      }
    }
    return authenticationService;
  }

  private AuthenticationServiceNoneImpl(Vertx vertx, JsonObject config) {

  }

  @Override
  public Future<MqttAuthResult> authenticate(MqttAuthData mqttAuthData) {

    return Future.succeededFuture(new MqttAuthResult(MqttConnectReturnCode.CONNECTION_ACCEPTED));
  }

}
