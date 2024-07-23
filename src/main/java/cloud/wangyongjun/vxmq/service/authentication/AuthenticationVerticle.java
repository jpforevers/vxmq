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

package cloud.wangyongjun.vxmq.service.authentication;

import cloud.wangyongjun.vxmq.assist.Config;
import cloud.wangyongjun.vxmq.assist.EBServices;
import cloud.wangyongjun.vxmq.service.authentication.impl.AuthenticationServiceNoneImpl;
import cloud.wangyongjun.vxmq.service.authentication.impl.AuthenticationServiceWebHookImpl;
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
      case WEBHOOK -> authenticationService = AuthenticationServiceWebHookImpl.getInstance(vertx);
      default -> authenticationService = AuthenticationServiceNoneImpl.getInstance(vertx);
    }
    new ServiceBinder(vertx.getDelegate()).setAddress(EBServices.AUTHENTICATION_SERVICE.getEbAddress())
      .registerLocal(AuthenticationService.class, authenticationService);
    return Uni.createFrom().voidItem();
  }

  @Override
  public Uni<Void> asyncStop() {
    return Uni.createFrom().voidItem();
  }

}
