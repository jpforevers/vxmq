package cloud.wangyongjun.vxmq.mqtt.msg;

import io.smallrye.mutiny.Uni;

import java.util.List;

public interface MsgService {

  Uni<Void> saveInboundQos2Pub(InboundQos2Pub inboundQos2Pub);

  Uni<InboundQos2Pub> removeInboundQos2Pub(String sessionId, int messageId);

  Uni<List<InboundQos2Pub>> allInboundQos2Pub();

  Uni<Long> countInboundQos2Pub();

  Uni<Void> saveOutboundQos1Pub(OutboundQos1Pub outboundQos1Pub);

  Uni<Boolean> removeOutboundQos1Pub(String sessionId, int messageId);

  Uni<List<OutboundQos1Pub>> allOutboundQos1Pub();

  Uni<List<OutboundQos1Pub>> outboundQos1Pub(String sessionId);

  Uni<Long> countOutboundQos1Pub();

  Uni<Void> saveOutboundQos2Pub(OutboundQos2Pub outboundQos2Pub);

  Uni<OutboundQos2Pub> removeOutboundQos2Pub(String sessionId, int messageId);

  Uni<List<OutboundQos2Pub>> allOutboundQos2Pub();

  Uni<List<OutboundQos2Pub>> outboundQos2Pub(String sessionId);

  Uni<Long> countOutboundQos2Pub();

  Uni<Void> saveOutboundQos2Rel(OutboundQos2Rel outboundQos2Rel);

  Uni<OutboundQos2Rel> removeOutboundQos2Rel(String sessionId, int messageId);

  Uni<List<OutboundQos2Rel>> allOutboundQos2Rel();

  Uni<List<OutboundQos2Rel>> outboundQos2Rel(String sessionId);

  Uni<Long> countOutboundQos2Rel();

  Uni<Void> saveOfflineMsg(MsgToClient msgToClient);

  Uni<List<MsgToClient>> allOfflineMsgOfSession(String sessionId);

  Uni<Void> clearMsgs(String sessionId);

}
