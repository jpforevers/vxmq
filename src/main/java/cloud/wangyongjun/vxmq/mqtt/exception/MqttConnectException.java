package cloud.wangyongjun.vxmq.mqtt.exception;

import io.netty.handler.codec.mqtt.MqttConnectReturnCode;

public class MqttConnectException extends MqttException {

  private final MqttConnectReturnCode code;

  /**
   * Suitable for MQTT version 3 and above.
   *
   * @param code code
   */
  public MqttConnectException(MqttConnectReturnCode code) {
    super("Connect failed: " + code);
    this.code = code;
  }

  public MqttConnectReturnCode getCode() {
    return code;
  }

}
