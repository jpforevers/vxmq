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

import cloud.wangyongjun.vxmq.service.msg.MsgToClient;
import io.smallrye.mutiny.Uni;
import java.util.List;

public interface ClientService {

  /**
   * Get client distributed lock
   * @param clientId clientId
   * @param timeout timeout
   * @return Void
   */
  Uni<Void> obtainClientLock(String clientId, long timeout);

  /**
   * Release client lock
   * @param clientId clientId
   */
  void releaseClientLock(String clientId);

  /**
   * Close {@link io.vertx.mqtt.MqttEndpoint}.
   *
   * @param clientVerticleId clientVerticleId
   * @return Void
   */
  Uni<Void> closeMqttEndpoint(String clientVerticleId);

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
  Uni<Void> undeployClientVerticle(String clientVerticleId);

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
