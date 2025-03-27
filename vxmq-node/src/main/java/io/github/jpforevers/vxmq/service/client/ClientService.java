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

package io.github.jpforevers.vxmq.service.client;

import io.github.jpforevers.vxmq.service.msg.MsgToClient;
import io.smallrye.mutiny.Uni;
import java.util.List;

public interface ClientService {

  /**
   * Get client distributed lock
   * @param key key
   * @param timeout timeout
   * @return Void
   */
  Uni<Void> obtainClientLock(String key, long timeout);

  /**
   * Release client lock
   * @param key key
   */
  void releaseClientLock(String key);

  /**
   * Close {@link io.vertx.mqtt.MqttEndpoint}.
   *
   * @param clientVerticleId clientVerticleId
   * @return Void
   */
  Uni<Void> closeMqttEndpoint(String clientVerticleId, CloseMqttEndpointRequest closeMqttEndpointRequest);

  /**
   * Send DISCONNECT to client.
   *
   * @param clientVerticleId  clientVerticleId
   * @param disconnectRequest disconnectRequest
   * @return Void
   */
  Uni<Void> disconnect(String clientVerticleId, DisconnectRequest disconnectRequest);

  /**
   * Undeploy client verticle.
   *
   * @param clientVerticleId clientVerticleId
   * @return Void
   */
  Uni<Void> undeployClientVerticle(String clientVerticleId, UndeployClientVerticleRequest undeployClientVerticleRequest);

  /**
   * Send PUBLISH to client.
   *
   * @param clientVerticleId clientVerticleId
   * @param msgToClient      msgToClient
   * @return Void
   */
  Uni<Void> sendPublish(String clientVerticleId, MsgToClient msgToClient);

  List<String> verticleIds();

}
