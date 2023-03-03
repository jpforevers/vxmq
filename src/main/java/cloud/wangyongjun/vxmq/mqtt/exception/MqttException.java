package cloud.wangyongjun.vxmq.mqtt.exception;

public class MqttException extends RuntimeException {

  public MqttException() {
  }

  public MqttException(String message) {
    super(message);
  }

  public MqttException(String message, Throwable cause) {
    super(message, cause);
  }

  public MqttException(Throwable cause) {
    super(cause);
  }

}
