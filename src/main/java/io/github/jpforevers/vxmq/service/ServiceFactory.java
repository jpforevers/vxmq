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

package io.github.jpforevers.vxmq.service;

import io.github.jpforevers.vxmq.event.DefaultEventService;
import io.github.jpforevers.vxmq.event.EventService;
import io.github.jpforevers.vxmq.service.alias.InboundTopicAliasService;
import io.github.jpforevers.vxmq.service.alias.OutboundTopicAliasService;
import io.github.jpforevers.vxmq.service.authentication.mutiny.AuthenticationService;
import io.github.jpforevers.vxmq.service.client.ClientService;
import io.github.jpforevers.vxmq.service.client.DefaultClientService;
import io.github.jpforevers.vxmq.service.composite.CompositeService;
import io.github.jpforevers.vxmq.service.composite.DefaultCompositeService;
import io.github.jpforevers.vxmq.service.msg.IgniteAndMapMsgService;
import io.github.jpforevers.vxmq.service.msg.MsgService;
import io.github.jpforevers.vxmq.service.retain.IgniteRetainService;
import io.github.jpforevers.vxmq.service.retain.RetainService;
import io.github.jpforevers.vxmq.service.session.IgniteSessionService;
import io.github.jpforevers.vxmq.service.session.SessionService;
import io.github.jpforevers.vxmq.service.sub.mutiny.SubService;
import io.github.jpforevers.vxmq.service.will.IgniteWillService;
import io.github.jpforevers.vxmq.service.will.WillService;
import io.vertx.mutiny.core.Vertx;

public class ServiceFactory {

  public static SessionService sessionService(Vertx vertx) {
    return IgniteSessionService.getSingleton(vertx);
  }

  public static SubService subService(Vertx vertx) {
    return VertxServiceProxyBuilder.buildSubService(vertx);
  }

  public static ClientService clientService(Vertx vertx) {
    return DefaultClientService.getSingleton(vertx);
  }

  public static MsgService msgService(Vertx vertx) {
//    return IgniteMsgService.getSingleton(vertx);
    return IgniteAndMapMsgService.getSingleton(vertx);
  }

  public static RetainService retainService(Vertx vertx) {
    return IgniteRetainService.getSingleton(vertx);
  }

  public static WillService willService(Vertx vertx) {
    return IgniteWillService.getSingleton(vertx);
  }

  public static CompositeService compositeService(Vertx vertx) {
    return DefaultCompositeService.getSingleton(vertx, sessionService(vertx), subService(vertx), willService(vertx),
      msgService(vertx), retainService(vertx), clientService(vertx), inboundTopicAliasService(vertx));
  }

  public static EventService eventService(Vertx vertx){
    return DefaultEventService.getSingleton(vertx);
  }

  public static AuthenticationService authenticationService(Vertx vertx){
    return VertxServiceProxyBuilder.buildAuthenticationService(vertx);
  }

  public static InboundTopicAliasService inboundTopicAliasService(Vertx vertx) {
    return InboundTopicAliasService.getSingleton(vertx, clientService(vertx), sessionService(vertx));
  }

  public static OutboundTopicAliasService outboundTopicAliasService(Vertx vertx) {
    return OutboundTopicAliasService.getSingleton(vertx);
  }

}
