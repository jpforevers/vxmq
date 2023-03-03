package cloud.wangyongjun.vxmq.mqtt.exception;

import io.vertx.mqtt.messages.codes.MqttSubAckReasonCode;

public class MqttSubscribeException extends MqttException {

  private final MqttSubAckReasonCode code;

  /**
   * Suitable for MQTT version 3 and above.
   *
   * @param code code
   */
  public MqttSubscribeException(MqttSubAckReasonCode code) {
    super(String.format("Subscribe failed: %s", code));
    this.code = code;
  }

  public MqttSubAckReasonCode code() {
    return this.code;
  }

}
