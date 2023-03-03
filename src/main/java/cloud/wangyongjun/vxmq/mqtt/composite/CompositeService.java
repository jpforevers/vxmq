package cloud.wangyongjun.vxmq.mqtt.composite;

import cloud.wangyongjun.vxmq.mqtt.msg.MsgToClient;
import cloud.wangyongjun.vxmq.mqtt.msg.MsgToTopic;
import cloud.wangyongjun.vxmq.mqtt.session.Session;
import io.smallrye.mutiny.Uni;

public interface CompositeService {

  /**
   * Clear session data: All messages, subscriptions, session self.
   *
   * @param clientId clientId
   * @return Void
   */
  Uni<Void> clearSession(String clientId);

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

}
