package cloud.wangyongjun.vxmq.mqtt.exception;

import io.vertx.mqtt.messages.codes.MqttPubAckReasonCode;
import io.vertx.mqtt.messages.codes.MqttPubRecReasonCode;

public class MqttPublishException extends MqttException {

  private MqttPubAckReasonCode mqttPubAckReasonCode;
  private MqttPubRecReasonCode mqttPubRecReasonCode;

  /**
   * Suitable for MQTT version 3 and above.
   *
   * @param message message
   */
  public MqttPublishException(String message) {
    super(message);
  }

  /**
   * Suitable for MQTT version above 3.
   *
   * @param code code
   */
  public MqttPublishException(MqttPubAckReasonCode code) {
    super(String.format("Publish failed: %s", code));
    this.mqttPubAckReasonCode = code;
  }

  /**
   * Suitable for MQTT version above 3.
   *
   * @param code code
   */
  public MqttPublishException(MqttPubRecReasonCode code) {
    super(String.format("Publish failed: %s", code));
    this.mqttPubRecReasonCode = code;
  }

  public MqttPubAckReasonCode mqttPubAckReasonCode() {
    return mqttPubAckReasonCode;
  }

  public MqttPubRecReasonCode mqttPubRecReasonCode() {
    return mqttPubRecReasonCode;
  }

}
