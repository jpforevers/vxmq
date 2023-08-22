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

package cloud.wangyongjun.vxmq.service.msg;

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
