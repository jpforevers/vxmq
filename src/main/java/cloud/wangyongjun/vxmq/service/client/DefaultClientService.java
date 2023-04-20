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

package cloud.wangyongjun.vxmq.service.client;

import cloud.wangyongjun.vxmq.assist.EBHeader;
import cloud.wangyongjun.vxmq.service.msg.MsgToClient;
import io.smallrye.mutiny.Uni;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.mutiny.core.Vertx;

public class DefaultClientService implements ClientService {

  private static volatile DefaultClientService defaultClientService;

  public static DefaultClientService getSingleton(Vertx vertx) {
    if (defaultClientService == null) {
      synchronized (DefaultClientService.class) {
        if (defaultClientService == null) {
          defaultClientService = new DefaultClientService(vertx);
        }
      }
    }
    return defaultClientService;
  }

  private final Vertx vertx;

  private DefaultClientService(Vertx vertx) {
    this.vertx = vertx;
  }

  @Override
  public Uni<Void> closeMqttEndpoint(String clientVerticleId) {
    DeliveryOptions deliveryOptions = new DeliveryOptions().addHeader(EBHeader.ACTION.name(), ClientVerticleAction.CLOSE_MQTT_ENDPOINT.name());
    return vertx.eventBus().sender(clientVerticleId, deliveryOptions).write(new JsonObject());
  }

  @Override
  public Uni<Void> disconnect(String clientVerticleId, DisconnectRequest disconnectRequest) {
    DeliveryOptions deliveryOptions = new DeliveryOptions().addHeader(EBHeader.ACTION.name(), ClientVerticleAction.DISCONNECT.name());
    return vertx.eventBus().sender(clientVerticleId, deliveryOptions).write(disconnectRequest.toJson());
  }

  @Override
  public Uni<Void> undeployClientVerticle(String clientVerticleId) {
    DeliveryOptions deliveryOptions = new DeliveryOptions().addHeader(EBHeader.ACTION.name(), ClientVerticleAction.UNDEPLOY_CLIENT_VERTICLE.name());
    return vertx.eventBus().sender(clientVerticleId, deliveryOptions).write(new JsonObject());
  }

  @Override
  public Uni<Void> sendPublish(String clientVerticleId, MsgToClient msgToClient) {
    DeliveryOptions deliveryOptions = new DeliveryOptions().addHeader(EBHeader.ACTION.name(), ClientVerticleAction.SEND_PUBLISH.name());
    return vertx.eventBus().sender(clientVerticleId, deliveryOptions).write(msgToClient.toJson());
  }

}
