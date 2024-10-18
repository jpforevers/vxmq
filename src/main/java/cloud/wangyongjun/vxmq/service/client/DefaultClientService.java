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
  public Uni<Void> closeMqttEndpoint(String clientVerticleId, CloseMqttEndpointRequest closeMqttEndpointRequest) {
    ToClientVerticleMsg toClientVerticleMsg = new ToClientVerticleMsg(ToClientVerticleMsg.Type.CLOSE_MQTT_ENDPOINT, closeMqttEndpointRequest);
    return vertx.eventBus().sender(clientVerticleId).write(toClientVerticleMsg);
  }

  @Override
  public Uni<Void> disconnect(String clientVerticleId, DisconnectRequest disconnectRequest) {
    ToClientVerticleMsg toClientVerticleMsg = new ToClientVerticleMsg(ToClientVerticleMsg.Type.DISCONNECT, disconnectRequest);
    return vertx.eventBus().sender(clientVerticleId).write(toClientVerticleMsg);
  }

  @Override
  public Uni<Void> undeployClientVerticle(String clientVerticleId, UndeployClientVerticleRequest undeployClientVerticleRequest) {
    ToClientVerticleMsg toClientVerticleMsg = new ToClientVerticleMsg(ToClientVerticleMsg.Type.UNDEPLOY_CLIENT_VERTICLE, undeployClientVerticleRequest);
    return vertx.eventBus().sender(clientVerticleId).write(toClientVerticleMsg);
  }

  @Override
  public Uni<Void> sendPublish(String clientVerticleId, MsgToClient msgToClient) {
    ToClientVerticleMsg toClientVerticleMsg = new ToClientVerticleMsg(ToClientVerticleMsg.Type.SEND_PUBLISH, msgToClient);
    return vertx.eventBus().sender(clientVerticleId).write(toClientVerticleMsg);
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
