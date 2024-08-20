package cloud.wangyongjun.vxmq;

import io.netty.handler.codec.mqtt.MqttConnectReturnCode;
import io.vertx.junit5.VertxTestContext;
import io.vertx.mqtt.MqttClientOptions;
import io.vertx.mutiny.core.Vertx;
import io.vertx.mutiny.mqtt.MqttClient;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestConnect extends BaseTest {

  private static final Logger LOGGER = LoggerFactory.getLogger(TestConnect.class);

  @Test
  void testMqttSpec311Connect(Vertx vertx, VertxTestContext testContext) throws Throwable {
    MqttClientOptions mqttClientOptions = new MqttClientOptions();

    MqttClient mqttClient = MqttClient.create(vertx, mqttClientOptions);
    mqttClient.connect(1883, "localhost")
      .onItem().invoke(mqttConnAckMessage -> {
        LOGGER.info("Mqtt client connected, code: {}", mqttConnAckMessage.code());
        assertEquals(MqttConnectReturnCode.CONNECTION_ACCEPTED, mqttConnAckMessage.code());
      })
      .onItem().transformToUni(v -> mqttClient.disconnect())
      .subscribe().with(v -> testContext.completeNow(), testContext::failNow);
  }

}
