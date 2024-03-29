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

package cloud.wangyongjun.vxmq.assist;

import cloud.wangyongjun.vxmq.service.authentication.mutiny.AuthenticationService;
import cloud.wangyongjun.vxmq.service.sub.mutiny.SubService;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.mutiny.core.Vertx;

public class VertxServiceProxyBuilder {

  public static SubService buildSubService(Vertx vertx) {
    io.vertx.serviceproxy.ServiceProxyBuilder builder = new io.vertx.serviceproxy.ServiceProxyBuilder(vertx.getDelegate())
      .setAddress(EBServices.SUB_SERVICE.getEbAddress())
      .setOptions(new DeliveryOptions().setLocalOnly(true));
    cloud.wangyongjun.vxmq.service.sub.SubService subService = builder.build(cloud.wangyongjun.vxmq.service.sub.SubService.class);
    return SubService.newInstance(subService);
  }

  public static AuthenticationService buildAuthenticationService(Vertx vertx){
    io.vertx.serviceproxy.ServiceProxyBuilder builder = new io.vertx.serviceproxy.ServiceProxyBuilder(vertx.getDelegate())
      .setAddress(EBServices.AUTHENTICATION_SERVICE.getEbAddress())
      .setOptions(new DeliveryOptions().setLocalOnly(true));
    cloud.wangyongjun.vxmq.service.authentication.AuthenticationService subService = builder.build(cloud.wangyongjun.vxmq.service.authentication.AuthenticationService.class);
    return AuthenticationService.newInstance(subService);
  }

}
