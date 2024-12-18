package io.github.jpforevers.vxmq.service.authentication.impl;

import io.github.jpforevers.vxmq.service.authentication.AuthenticationService;
import io.github.jpforevers.vxmq.service.authentication.MqttAuthData;
import io.github.jpforevers.vxmq.service.authentication.MqttAuthResult;
import io.netty.handler.codec.mqtt.MqttConnectReturnCode;
import io.vertx.core.Future;
import io.vertx.mutiny.core.Vertx;

public class AuthenticationServiceNoneImpl implements AuthenticationService {

  private static volatile AuthenticationService authenticationService;

  public static AuthenticationService getInstance(Vertx vertx) {
    if (authenticationService == null) {
      synchronized (AuthenticationServiceNoneImpl.class) {
        if (authenticationService == null) {
          authenticationService = new AuthenticationServiceNoneImpl(vertx);
        }
      }
    }
    return authenticationService;
  }

  private AuthenticationServiceNoneImpl(Vertx vertx) {

  }

  @Override
  public Future<MqttAuthResult> authenticate(MqttAuthData mqttAuthData) {

    return Future.succeededFuture(new MqttAuthResult(MqttConnectReturnCode.CONNECTION_ACCEPTED));
  }

}
