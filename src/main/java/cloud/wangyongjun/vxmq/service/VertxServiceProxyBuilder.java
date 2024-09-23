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

package cloud.wangyongjun.vxmq.service;

import cloud.wangyongjun.vxmq.assist.EBServices;
import cloud.wangyongjun.vxmq.service.authentication.mutiny.AuthenticationService;
import cloud.wangyongjun.vxmq.service.sub.mutiny.SubService;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.mutiny.core.Vertx;

class VertxServiceProxyBuilder {

  static SubService buildSubService(Vertx vertx) {
    io.vertx.serviceproxy.ServiceProxyBuilder builder = new io.vertx.serviceproxy.ServiceProxyBuilder(vertx.getDelegate())
      .setAddress(EBServices.SUB_SERVICE.getEbAddress())
      .setOptions(new DeliveryOptions().setLocalOnly(true));
    cloud.wangyongjun.vxmq.service.sub.SubService subService = builder.build(cloud.wangyongjun.vxmq.service.sub.SubService.class);
    return SubService.newInstance(subService);
  }

  static AuthenticationService buildAuthenticationService(Vertx vertx){
    io.vertx.serviceproxy.ServiceProxyBuilder builder = new io.vertx.serviceproxy.ServiceProxyBuilder(vertx.getDelegate())
      .setAddress(EBServices.AUTHENTICATION_SERVICE.getEbAddress())
      .setOptions(new DeliveryOptions().setLocalOnly(true));
    cloud.wangyongjun.vxmq.service.authentication.AuthenticationService subService = builder.build(cloud.wangyongjun.vxmq.service.authentication.AuthenticationService.class);
    return AuthenticationService.newInstance(subService);
  }

}
