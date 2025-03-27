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

package io.github.jpforevers.vxmq.service.msg;

import io.smallrye.mutiny.Uni;

import java.util.List;

public interface MsgService {

  Uni<Void> saveInboundQos2Pub(InboundQos2Pub inboundQos2Pub);

  Uni<InboundQos2Pub> getAndRemoveInboundQos2Pub(String sessionId, int messageId);

  Uni<List<InboundQos2Pub>> allInboundQos2Pub();

  Uni<Long> countInboundQos2Pub();

  Uni<Void> saveOutboundQos1Pub(OutboundQos1Pub outboundQos1Pub);

  Uni<OutboundQos1Pub> getAndRemoveOutboundQos1Pub(String sessionId, int messageId);

  Uni<List<OutboundQos1Pub>> allOutboundQos1Pub();

  Uni<List<OutboundQos1Pub>> outboundQos1Pub(String sessionId);

  Uni<Long> countOutboundQos1Pub();

  Uni<Void> saveOutboundQos2Pub(OutboundQos2Pub outboundQos2Pub);

  Uni<OutboundQos2Pub> getAndRemoveOutboundQos2Pub(String sessionId, int messageId);

  Uni<List<OutboundQos2Pub>> allOutboundQos2Pub();

  Uni<List<OutboundQos2Pub>> outboundQos2Pub(String sessionId);

  Uni<Long> countOutboundQos2Pub();

  Uni<Void> saveOutboundQos2Rel(OutboundQos2Rel outboundQos2Rel);

  Uni<OutboundQos2Rel> getAndRemoveOutboundQos2Rel(String sessionId, int messageId);

  Uni<List<OutboundQos2Rel>> allOutboundQos2Rel();

  Uni<List<OutboundQos2Rel>> outboundQos2Rel(String sessionId);

  Uni<Long> countOutboundQos2Rel();

  Uni<Void> saveOfflineMsg(MsgToClient msgToClient);

  Uni<List<MsgToClient>> allOfflineMsgOfSession(String sessionId);

  Uni<Void> clearMsgs(String sessionId);

}
