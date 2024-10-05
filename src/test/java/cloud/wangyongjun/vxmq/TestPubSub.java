package cloud.wangyongjun.vxmq;

import com.hivemq.client.mqtt.datatypes.MqttQos;
import com.hivemq.client.mqtt.mqtt3.Mqtt3AsyncClient;
import com.hivemq.client.mqtt.mqtt3.Mqtt3Client;
import com.hivemq.client.mqtt.mqtt3.message.publish.Mqtt3Publish;
import com.hivemq.client.mqtt.mqtt3.message.subscribe.Mqtt3Subscribe;
import io.vertx.junit5.Checkpoint;
import io.vertx.junit5.VertxTestContext;
import io.vertx.mutiny.core.Vertx;
import org.junit.jupiter.api.Test;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestPubSub extends BaseTest {

  @Test
  public void testMqtt311PubSubSuccess(Vertx vertx, VertxTestContext testContext) {
    Checkpoint checkpoint = testContext.checkpoint(3);
    AtomicInteger atomicInteger = new AtomicInteger();
    Mqtt3AsyncClient mqtt3SubAsyncClient = Mqtt3Client.builder()
      .identifier("subClient")
      .buildAsync();
    Mqtt3AsyncClient mqtt3PubAsyncClient = Mqtt3Client.builder()
      .identifier("pubClient")
      .buildAsync();
    mqtt3SubAsyncClient.connect()
      .thenCompose(v -> {
        Mqtt3Subscribe mqtt3Subscribe = Mqtt3Subscribe.builder()
          .topicFilter("abc/def/123")
          .qos(MqttQos.AT_LEAST_ONCE)
          .build();
        return mqtt3SubAsyncClient.subscribe(mqtt3Subscribe, mqtt3Publish -> {
          int index = atomicInteger.addAndGet(1);
          if (index == 1) {
            assertEquals("p1", Charset.defaultCharset().decode(mqtt3Publish.getPayload().get()).toString());
            checkpoint.flag();
          }
          if (index == 2) {
            assertEquals("p2", Charset.defaultCharset().decode(mqtt3Publish.getPayload().get()).toString());
            checkpoint.flag();
          }
          if (index == 3) {
            assertEquals("p3", Charset.defaultCharset().decode(mqtt3Publish.getPayload().get()).toString());
            checkpoint.flag();
          }
        });
      })
      .thenCompose(v -> mqtt3PubAsyncClient.connect())
      .thenCompose(v -> mqtt3PubAsyncClient.publish(Mqtt3Publish.builder().topic("abc/def/123").qos(MqttQos.AT_MOST_ONCE).payload("p1".getBytes(StandardCharsets.UTF_8)).build()))
      .thenCompose(v -> mqtt3PubAsyncClient.publish(Mqtt3Publish.builder().topic("abc/def/123").qos(MqttQos.AT_LEAST_ONCE).payload("p2".getBytes(StandardCharsets.UTF_8)).build()))
      .thenCompose(v -> mqtt3PubAsyncClient.publish(Mqtt3Publish.builder().topic("abc/def/123").qos(MqttQos.EXACTLY_ONCE).payload("p3".getBytes(StandardCharsets.UTF_8)).build()))
      .whenComplete((v, t) -> {
        if (t != null) {
          testContext.failNow(t);
        }
      });
  }

  @Test
  public void testMqtt311PubSubSingleLevelWildcard(Vertx vertx, VertxTestContext testContext) {
    Checkpoint checkpoint = testContext.checkpoint(9);
    Mqtt3AsyncClient mqtt3SubAsyncClient1 = Mqtt3Client.builder()
      .identifier("subClient1")
      .buildAsync();
    Mqtt3AsyncClient mqtt3SubAsyncClient2 = Mqtt3Client.builder()
      .identifier("subClient2")
      .buildAsync();
    Mqtt3AsyncClient mqtt3SubAsyncClient3 = Mqtt3Client.builder()
      .identifier("subClient3")
      .buildAsync();
    Mqtt3AsyncClient mqtt3PubAsyncClient = Mqtt3Client.builder()
      .identifier("pubClient")
      .buildAsync();
    mqtt3SubAsyncClient1.connect()
      .thenCompose(v -> {
        Mqtt3Subscribe mqtt3Subscribe = Mqtt3Subscribe.builder()
          .topicFilter("+/def/123")
          .qos(MqttQos.AT_LEAST_ONCE)
          .build();
        return mqtt3SubAsyncClient1.subscribe(mqtt3Subscribe, mqtt3Publish -> {
          checkpoint.flag();
        });
      })
      .thenCompose(v -> mqtt3SubAsyncClient2.connect())
      .thenCompose(v -> {
        Mqtt3Subscribe mqtt3Subscribe = Mqtt3Subscribe.builder()
          .topicFilter("abc/+/123")
          .qos(MqttQos.AT_LEAST_ONCE)
          .build();
        return mqtt3SubAsyncClient2.subscribe(mqtt3Subscribe, mqtt3Publish -> {
          checkpoint.flag();
        });
      })
      .thenCompose(v -> mqtt3SubAsyncClient3.connect())
      .thenCompose(v -> {
        Mqtt3Subscribe mqtt3Subscribe = Mqtt3Subscribe.builder()
          .topicFilter("abc/def/+")
          .qos(MqttQos.AT_LEAST_ONCE)
          .build();
        return mqtt3SubAsyncClient3.subscribe(mqtt3Subscribe, mqtt3Publish -> {
          checkpoint.flag();
        });
      })
      .thenCompose(v -> mqtt3PubAsyncClient.connect())
      .thenCompose(v -> mqtt3PubAsyncClient.publish(Mqtt3Publish.builder().topic("abc/def/123").qos(MqttQos.AT_MOST_ONCE).payload("p1".getBytes(StandardCharsets.UTF_8)).build()))
      .thenCompose(v -> mqtt3PubAsyncClient.publish(Mqtt3Publish.builder().topic("abc/def/123").qos(MqttQos.AT_LEAST_ONCE).payload("p2".getBytes(StandardCharsets.UTF_8)).build()))
      .thenCompose(v -> mqtt3PubAsyncClient.publish(Mqtt3Publish.builder().topic("abc/def/123").qos(MqttQos.EXACTLY_ONCE).payload("p3".getBytes(StandardCharsets.UTF_8)).build()))
      .whenComplete((v, t) -> {
        if (t != null) {
          testContext.failNow(t);
        }
      });
  }

  @Test
  public void testMqtt311PubSubMultiLevelWildcard(Vertx vertx, VertxTestContext testContext) {
    Checkpoint checkpoint = testContext.checkpoint(3);
    AtomicInteger atomicInteger = new AtomicInteger();
    Mqtt3AsyncClient mqtt3SubAsyncClient = Mqtt3Client.builder()
      .identifier("subClient")
      .buildAsync();
    Mqtt3AsyncClient mqtt3PubAsyncClient = Mqtt3Client.builder()
      .identifier("pubClient")
      .buildAsync();
    mqtt3SubAsyncClient.connect()
      .thenCompose(v -> {
        Mqtt3Subscribe mqtt3Subscribe = Mqtt3Subscribe.builder()
          .topicFilter("abc/#")
          .qos(MqttQos.AT_LEAST_ONCE)
          .build();
        return mqtt3SubAsyncClient.subscribe(mqtt3Subscribe, mqtt3Publish -> {
          int index = atomicInteger.addAndGet(1);
          if (index == 1) {
            assertEquals("p1", Charset.defaultCharset().decode(mqtt3Publish.getPayload().get()).toString());
            checkpoint.flag();
          }
          if (index == 2) {
            assertEquals("p2", Charset.defaultCharset().decode(mqtt3Publish.getPayload().get()).toString());
            checkpoint.flag();
          }
          if (index == 3) {
            assertEquals("p3", Charset.defaultCharset().decode(mqtt3Publish.getPayload().get()).toString());
            checkpoint.flag();
          }
        });
      })
      .thenCompose(v -> mqtt3PubAsyncClient.connect())
      .thenCompose(v -> mqtt3PubAsyncClient.publish(Mqtt3Publish.builder().topic("abc/def/123").qos(MqttQos.AT_MOST_ONCE).payload("p1".getBytes(StandardCharsets.UTF_8)).build()))
      .thenCompose(v -> mqtt3PubAsyncClient.publish(Mqtt3Publish.builder().topic("abc/def/123").qos(MqttQos.AT_LEAST_ONCE).payload("p2".getBytes(StandardCharsets.UTF_8)).build()))
      .thenCompose(v -> mqtt3PubAsyncClient.publish(Mqtt3Publish.builder().topic("abc/def/123").qos(MqttQos.EXACTLY_ONCE).payload("p3".getBytes(StandardCharsets.UTF_8)).build()))
      .whenComplete((v, t) -> {
        if (t != null) {
          testContext.failNow(t);
        }
      });
  }

}
