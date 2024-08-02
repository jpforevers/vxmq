package cloud.wangyongjun.vxmq;

import io.netty.handler.codec.mqtt.MqttConnectReturnCode;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import io.vertx.mqtt.MqttClientOptions;
import io.vertx.mutiny.core.Vertx;
import io.vertx.mutiny.mqtt.MqttClient;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(VertxExtension.class)
public class TestConnect {

  private static final Logger LOGGER = LoggerFactory.getLogger(TestConnect.class);

  @BeforeAll
  static void startServer(Vertx vertx, VertxTestContext testContext) {
    VxmqLauncher vxmqLauncher = new VxmqLauncher();
    vxmqLauncher.start().subscribe().with(v -> testContext.completeNow(), testContext::failNow);
  }

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
    assertTrue(testContext.awaitCompletion(2, TimeUnit.SECONDS));
    if (testContext.failed()) {
      throw testContext.causeOfFailure();
    }
  }

}
