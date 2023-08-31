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

package cloud.wangyongjun.vxmq.service.composite;

import cloud.wangyongjun.vxmq.service.msg.MsgToClient;
import cloud.wangyongjun.vxmq.service.msg.MsgToTopic;
import cloud.wangyongjun.vxmq.service.session.Session;
import io.smallrye.mutiny.Uni;

public interface CompositeService {

  /**
   * Clear session data: All messages, subscriptions, session self.
   *
   * @param clientId clientId
   * @return Void
   */
  Uni<Void> clearSessionData(String clientId);

  /**
   * Publish will.
   *
   * @param sessionId sessionId
   * @return Void
   */
  Uni<Void> publishWill(String sessionId);

  /**
   * Send message to client
   *
   * @param session     session
   * @param msgToClient msgToClient
   * @return Void
   */
  Uni<Void> sendToClient(Session session, MsgToClient msgToClient);

  /**
   * Forward message to topic.
   *
   * @param msgToTopic msgToTopic
   * @return Void
   */
  Uni<Void> forward(MsgToTopic msgToTopic);

  /**
   * Send offline message
   *
   * @param sessionId sessionId
   * @return Void
   */
  Uni<Void> sendOfflineMsg(String sessionId);

  /**
   * Kickoff client connection if connected, clear session data, delete session self.
   * @param clientId clientId
   * @return Void
   */
  Uni<Void> deleteSession(String clientId);

}
