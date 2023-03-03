package cloud.wangyongjun.vxmq.mqtt.client;

import cloud.wangyongjun.vxmq.mqtt.msg.MsgToClient;
import io.smallrye.mutiny.Uni;

public interface ClientService {

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

}
