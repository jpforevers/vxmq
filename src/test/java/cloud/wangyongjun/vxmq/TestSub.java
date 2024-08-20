package cloud.wangyongjun.vxmq;

import cloud.wangyongjun.vxmq.service.sub.Subscription;
import io.netty.handler.codec.mqtt.MqttConnectReturnCode;
import io.smallrye.mutiny.Uni;
import io.vertx.junit5.VertxTestContext;
import io.vertx.mqtt.MqttClientOptions;
import io.vertx.mutiny.core.Vertx;
import io.vertx.mutiny.mqtt.MqttClient;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestSub extends BaseTest {

  private static final Logger LOGGER = LoggerFactory.getLogger(TestSub.class);

  @Test
  void testSub(Vertx vertx, VertxTestContext testContext) throws Throwable {
    Map<String, Integer> topicToQosMap = new HashMap<>();
    for (Subscription subscription : TestConstants.SUBSCRIPTIONS) {
      topicToQosMap.put(subscription.getTopicFilter(), subscription.getQos());
    }

    MqttClientOptions mqttClientOptions = new MqttClientOptions();
    MqttClient mqttClient = MqttClient.create(vertx, mqttClientOptions);
    mqttClient.connect(1883, "localhost")
      .onItem().invoke(mqttConnAckMessage -> {
        LOGGER.info("Mqtt client connected, code: {}", mqttConnAckMessage.code());
        assertEquals(MqttConnectReturnCode.CONNECTION_ACCEPTED, mqttConnAckMessage.code());
      })
      .replaceWithVoid()
      .onItem().transformToUni(v -> {
        List<Uni<Integer>> unis = new ArrayList<>();
        for (Map.Entry<String, Integer> stringIntegerEntry : topicToQosMap.entrySet()) {
          Uni<Integer> uni = mqttClient.subscribe(stringIntegerEntry.getKey(), stringIntegerEntry.getValue())
            .onItem().invoke(i -> LOGGER.info("Mqtt client subscribed, topic: {}, qos: {}", stringIntegerEntry.getKey(), stringIntegerEntry.getValue()));
          unis.add(uni);
        }
        return Uni.combine().all().unis(unis).collectFailures().discardItems();
      })
      .onItem().transformToUni(v -> mqttClient.disconnect())
      .subscribe().with(v -> testContext.completeNow(), testContext::failNow);
  }

}
