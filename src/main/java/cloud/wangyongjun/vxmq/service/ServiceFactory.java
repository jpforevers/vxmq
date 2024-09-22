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

package cloud.wangyongjun.vxmq.service;

import cloud.wangyongjun.vxmq.event.DefaultEventService;
import cloud.wangyongjun.vxmq.event.EventService;
import cloud.wangyongjun.vxmq.service.alias.InboundTopicAliasService;
import cloud.wangyongjun.vxmq.service.authentication.mutiny.AuthenticationService;
import cloud.wangyongjun.vxmq.service.client.ClientService;
import cloud.wangyongjun.vxmq.service.client.DefaultClientService;
import cloud.wangyongjun.vxmq.service.composite.CompositeService;
import cloud.wangyongjun.vxmq.service.composite.DefaultCompositeService;
import cloud.wangyongjun.vxmq.service.msg.IgniteAndMapMsgService;
import cloud.wangyongjun.vxmq.service.msg.MsgService;
import cloud.wangyongjun.vxmq.service.retain.IgniteRetainService;
import cloud.wangyongjun.vxmq.service.retain.RetainService;
import cloud.wangyongjun.vxmq.service.session.IgniteSessionService;
import cloud.wangyongjun.vxmq.service.session.SessionService;
import cloud.wangyongjun.vxmq.service.sub.mutiny.SubService;
import cloud.wangyongjun.vxmq.service.will.IgniteWillService;
import cloud.wangyongjun.vxmq.service.will.WillService;
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
}
