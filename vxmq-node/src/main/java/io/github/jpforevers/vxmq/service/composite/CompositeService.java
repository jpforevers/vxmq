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

package io.github.jpforevers.vxmq.service.composite;

import io.github.jpforevers.vxmq.service.msg.MsgToClient;
import io.github.jpforevers.vxmq.service.msg.MsgToTopic;
import io.github.jpforevers.vxmq.service.session.Session;
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
