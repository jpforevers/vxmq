/*
 * Copyright (C) 2023-2024 王用军
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package io.github.jpforevers.vxmq.service.authentication;

import io.github.jpforevers.vxmq.assist.Config;
import io.github.jpforevers.vxmq.assist.EBFactory;
import io.github.jpforevers.vxmq.service.authentication.impl.AuthenticationServiceNoneImpl;
import io.github.jpforevers.vxmq.service.authentication.impl.AuthenticationServiceWebHookImpl;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.vertx.core.AbstractVerticle;
import io.vertx.serviceproxy.ServiceBinder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AuthenticationVerticle extends AbstractVerticle {

  private static final Logger LOGGER = LoggerFactory.getLogger(AuthenticationVerticle.class);

  @Override
  public Uni<Void> asyncStart() {
    MqttAuthType mqttAuthType = Config.getMqttAuthType();
    LOGGER.info("Mqtt auth type: " + mqttAuthType);
    AuthenticationService authenticationService;
    switch (mqttAuthType){
      case NONE -> authenticationService = AuthenticationServiceNoneImpl.getInstance(vertx);
      case WEBHOOK -> authenticationService = AuthenticationServiceWebHookImpl.getInstance(vertx, Config.getMqttAuthWebhookUrl());
      default -> authenticationService = AuthenticationServiceNoneImpl.getInstance(vertx);
    }
    new ServiceBinder(vertx.getDelegate()).setAddress(EBFactory.EBServices.AUTHENTICATION_SERVICE.getEbAddress())
      .registerLocal(AuthenticationService.class, authenticationService);
    return Uni.createFrom().voidItem();
  }

  @Override
  public Uni<Void> asyncStop() {
    return Uni.createFrom().voidItem();
  }

}
