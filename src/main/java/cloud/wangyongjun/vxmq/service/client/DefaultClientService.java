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
import cloud.wangyongjun.vxmq.assist.VertxUtil;
import cloud.wangyongjun.vxmq.service.msg.MsgToClient;
import io.smallrye.mutiny.Uni;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.impl.Deployment;
import io.vertx.core.impl.VertxInternal;
import io.vertx.core.json.JsonObject;
import io.vertx.mutiny.core.Vertx;
import io.vertx.mutiny.core.shareddata.Lock;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultClientService implements ClientService {

  private final static Logger LOGGER = LoggerFactory.getLogger(DefaultClientService.class);

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
  private final Map<String, Lock> clientLockMap = new ConcurrentHashMap<>();

  private DefaultClientService(Vertx vertx) {
    this.vertx = vertx;
  }

  @Override
  public Uni<Void> obtainClientLock(String clientId, long timeout) {
    return vertx.sharedData().getLockWithTimeout(clientId, timeout)
      .onItem().invoke(lock -> clientLockMap.put(clientId, lock))
      .onItem().invoke(lock -> {
        if (LOGGER.isDebugEnabled()) {
          LOGGER.debug("Client lock obtained for {}", clientId);
        }
      })
      .replaceWithVoid();
  }

  @Override
  public void releaseClientLock(String clientId) {
    Lock lock = clientLockMap.get(clientId);
    if (lock != null) {
      lock.release();
      clientLockMap.remove(clientId);
    }
    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("Client lock released for {}", clientId);
    }
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

  @Override
  public List<String> verticleIds() {
    List<String> verticleIds = new ArrayList<>();
    for (String id : vertx.deploymentIDs()) {
      VertxInternal vertxInternal = VertxUtil.getVertxInternal(vertx);
      Deployment deployment = vertxInternal.getDeployment(id);
      String verticleIdentifier = deployment.verticleIdentifier();  // like "java:cloud.wangyongjun.vxmq.service.client.ClientVerticle"
      if (StringUtils.substringAfter(verticleIdentifier, ":").equals(ClientVerticle.class.getName())) {
        verticleIds.add(id);
      }
    }
    return verticleIds;
  }

}
