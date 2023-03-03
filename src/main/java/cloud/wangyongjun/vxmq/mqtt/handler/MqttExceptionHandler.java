package cloud.wangyongjun.vxmq.mqtt.handler;

import io.vertx.mutiny.mqtt.MqttEndpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Consumer;

/**
 * This will be called when an error at protocol level happens
 */
public class MqttExceptionHandler implements Consumer<Throwable> {

  private final static Logger LOGGER = LoggerFactory.getLogger(MqttExceptionHandler.class);

  private final MqttEndpoint mqttEndpoint;

  public MqttExceptionHandler(MqttEndpoint mqttEndpoint) {
    this.mqttEndpoint = mqttEndpoint;
  }

  @Override
  public void accept(Throwable throwable) {
    LOGGER.error("Error occurred at protocol level of " + mqttEndpoint.clientIdentifier(), throwable);
  }

}
