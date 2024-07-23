package cloud.wangyongjun.vxmq;

import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import io.vertx.mqtt.MqttClientOptions;
import io.vertx.mutiny.core.Vertx;
import io.vertx.mutiny.mqtt.MqttClient;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(VertxExtension.class)
public class TestConnect {

  @BeforeAll
  static void startServer(Vertx vertx, VertxTestContext testContext) {
    VxmqLauncher.main(new String[]{});
  }

  @Test
  void testMqttSpec311Connect(Vertx vertx, VertxTestContext testContext) throws Throwable {
    MqttClientOptions mqttClientOptions = new MqttClientOptions();

    MqttClient mqttClient = MqttClient.create(vertx, mqttClientOptions);
    testContext.completeNow();
  }

}
