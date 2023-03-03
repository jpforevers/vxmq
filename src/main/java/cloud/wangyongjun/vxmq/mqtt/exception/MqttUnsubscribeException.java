package cloud.wangyongjun.vxmq.mqtt.exception;

import io.vertx.mqtt.messages.codes.MqttUnsubAckReasonCode;

public class MqttUnsubscribeException extends MqttException {

  private MqttUnsubAckReasonCode code;

  /**
   * Suitable for MQTT version 3.
   *
   * @param message message
   */
  public MqttUnsubscribeException(String message) {
    super(message);
  }

  /**
   * Suitable for MQTT version above 3.
   *
   * @param code code
   */
  public MqttUnsubscribeException(MqttUnsubAckReasonCode code) {
    super(String.format("Unsubscribe failed: %s", code));
    this.code = code;
  }

  public MqttUnsubAckReasonCode code() {
    return this.code;
  }

}
