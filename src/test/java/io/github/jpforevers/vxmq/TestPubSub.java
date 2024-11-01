package io.github.jpforevers.vxmq;

import com.hivemq.client.mqtt.datatypes.MqttQos;
import com.hivemq.client.mqtt.mqtt3.Mqtt3AsyncClient;
import com.hivemq.client.mqtt.mqtt3.Mqtt3Client;
import com.hivemq.client.mqtt.mqtt3.message.publish.Mqtt3Publish;
import com.hivemq.client.mqtt.mqtt3.message.subscribe.Mqtt3Subscribe;
import com.hivemq.client.mqtt.mqtt5.Mqtt5AsyncClient;
import com.hivemq.client.mqtt.mqtt5.Mqtt5Client;
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5Publish;
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5PublishResult;
import com.hivemq.client.mqtt.mqtt5.message.subscribe.Mqtt5Subscribe;
import io.vertx.junit5.Checkpoint;
import io.vertx.junit5.VertxTestContext;
import io.vertx.mutiny.core.Vertx;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestPubSub extends BaseTest {

  private static final Logger LOGGER = LoggerFactory.getLogger(TestPubSub.class);

  @Test
  public void testMqtt311PubSubSuccess(Vertx vertx, VertxTestContext testContext) {
    Checkpoint checkpoint = testContext.checkpoint(3);
    AtomicInteger publishReceivedCounter = new AtomicInteger();
    Mqtt3AsyncClient mqtt3SubAsyncClient = Mqtt3Client.builder().identifier("subClient").buildAsync();
    Mqtt3AsyncClient mqtt3PubAsyncClient = Mqtt3Client.builder().identifier("pubClient").buildAsync();
    mqtt3SubAsyncClient.connect()
      .thenCompose(v -> {
        Mqtt3Subscribe mqtt3Subscribe = Mqtt3Subscribe.builder().topicFilter("abc/def/123").qos(MqttQos.AT_LEAST_ONCE).build();
        return mqtt3SubAsyncClient.subscribe(mqtt3Subscribe, mqtt3Publish -> {
          int index = publishReceivedCounter.incrementAndGet();
          String payload = new String(mqtt3Publish.getPayloadAsBytes(), StandardCharsets.UTF_8);
          LOGGER.info("PUBLISH received, index: {}, topic: {}, qos: {}, payload: {}", index, mqtt3Publish.getTopic(), mqtt3Publish.getQos().getCode(), payload);
          if (index == 1) {
            assertEquals("p1", payload);
          }
          if (index == 2) {
            assertEquals("p2", payload);
          }
          if (index == 3) {
            assertEquals("p3", payload);
          }
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
  public void testMqtt311PubSubSingleLevelWildcard(Vertx vertx, VertxTestContext testContext) {
    Checkpoint checkpoint = testContext.checkpoint(3);
    AtomicInteger publishReceivedCounter1 = new AtomicInteger();
    AtomicInteger publishReceivedCounter2 = new AtomicInteger();
    AtomicInteger publishReceivedCounter3 = new AtomicInteger();
    Mqtt3AsyncClient mqtt3SubAsyncClient1 = Mqtt3Client.builder().identifier("subClient1").buildAsync();
    Mqtt3AsyncClient mqtt3SubAsyncClient2 = Mqtt3Client.builder().identifier("subClient2").buildAsync();
    Mqtt3AsyncClient mqtt3SubAsyncClient3 = Mqtt3Client.builder().identifier("subClient3").buildAsync();
    Mqtt3AsyncClient mqtt3PubAsyncClient = Mqtt3Client.builder().identifier("pubClient").buildAsync();
    mqtt3SubAsyncClient1.connect()
      .thenCompose(v -> {
        Mqtt3Subscribe mqtt3Subscribe = Mqtt3Subscribe.builder().topicFilter("+/def/123").qos(MqttQos.AT_LEAST_ONCE).build();
        return mqtt3SubAsyncClient1.subscribe(mqtt3Subscribe, mqtt3Publish -> {
          int index = publishReceivedCounter1.incrementAndGet();
          String payload = new String(mqtt3Publish.getPayloadAsBytes(), StandardCharsets.UTF_8);
          LOGGER.info("PUBLISH received, index: {}, topic: {}, qos: {}, payload: {}", index, mqtt3Publish.getTopic(), mqtt3Publish.getQos().getCode(), payload);
          if (index == 3) {
            checkpoint.flag();
          }
        });
      })
      .thenCompose(v -> mqtt3SubAsyncClient2.connect())
      .thenCompose(v -> {
        Mqtt3Subscribe mqtt3Subscribe = Mqtt3Subscribe.builder().topicFilter("abc/+/123").qos(MqttQos.AT_LEAST_ONCE).build();
        return mqtt3SubAsyncClient2.subscribe(mqtt3Subscribe, mqtt3Publish -> {
          int index = publishReceivedCounter2.incrementAndGet();
          String payload = new String(mqtt3Publish.getPayloadAsBytes(), StandardCharsets.UTF_8);
          LOGGER.info("PUBLISH received, index: {}, topic: {}, qos: {}, payload: {}", index, mqtt3Publish.getTopic(), mqtt3Publish.getQos().getCode(), payload);
          if (index == 3) {
            checkpoint.flag();
          }
        });
      })
      .thenCompose(v -> mqtt3SubAsyncClient3.connect())
      .thenCompose(v -> {
        Mqtt3Subscribe mqtt3Subscribe = Mqtt3Subscribe.builder().topicFilter("abc/def/+").qos(MqttQos.AT_LEAST_ONCE).build();
        return mqtt3SubAsyncClient3.subscribe(mqtt3Subscribe, mqtt3Publish -> {
          int index = publishReceivedCounter3.incrementAndGet();
          String payload = new String(mqtt3Publish.getPayloadAsBytes(), StandardCharsets.UTF_8);
          LOGGER.info("PUBLISH received, index: {}, topic: {}, qos: {}, payload: {}", index, mqtt3Publish.getTopic(), mqtt3Publish.getQos().getCode(), payload);
          if (index == 3) {
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
  public void testMqtt311PubSubMultiLevelWildcard(Vertx vertx, VertxTestContext testContext) {
    Checkpoint checkpoint = testContext.checkpoint(3);
    AtomicInteger publishReceivedCounter = new AtomicInteger();
    Mqtt3AsyncClient mqtt3SubAsyncClient = Mqtt3Client.builder().identifier("subClient").buildAsync();
    Mqtt3AsyncClient mqtt3PubAsyncClient = Mqtt3Client.builder().identifier("pubClient").buildAsync();
    mqtt3SubAsyncClient.connect()
      .thenCompose(v -> {
        Mqtt3Subscribe mqtt3Subscribe = Mqtt3Subscribe.builder().topicFilter("abc/#").qos(MqttQos.AT_LEAST_ONCE).build();
        return mqtt3SubAsyncClient.subscribe(mqtt3Subscribe, mqtt3Publish -> {
          int index = publishReceivedCounter.incrementAndGet();
          String payload = new String(mqtt3Publish.getPayloadAsBytes(), StandardCharsets.UTF_8);
          LOGGER.info("PUBLISH received, index: {}, topic: {}, qos: {}, payload: {}", index, mqtt3Publish.getTopic(), mqtt3Publish.getQos().getCode(), payload);
          if (index == 1) {
            assertEquals("p1", payload);
          }
          if (index == 2) {
            assertEquals("p2", payload);
          }
          if (index == 3) {
            assertEquals("p3", payload);
          }
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
  public void testMqtt5PubSharedSub(Vertx vertx, VertxTestContext testContext) {
    Checkpoint checkpoint = testContext.checkpoint(4);
    AtomicInteger publishReceivedCounter1 = new AtomicInteger();
    AtomicInteger publishReceivedCounter2 = new AtomicInteger();
    AtomicInteger publishReceivedCounter3 = new AtomicInteger();
    AtomicInteger publishReceivedCounter4 = new AtomicInteger();
    String share1SubClient1 = "share1SubClient1";
    Mqtt5AsyncClient mqtt5Share1SubClient1 = Mqtt5Client.builder().identifier(share1SubClient1).buildAsync();
    String share1SubClient2 = "share1SubClient2";
    Mqtt5AsyncClient mqtt5SubAsyncClient2 = Mqtt5Client.builder().identifier(share1SubClient2).buildAsync();
    String share2SubClient1 = "share2SubClient1";
    Mqtt5AsyncClient mqtt5Share2SubClient1 = Mqtt5Client.builder().identifier(share2SubClient1).buildAsync();
    String nonShareSubClient = "nonShareSubClient";
    Mqtt5AsyncClient mqtt5SubAsyncClient4 = Mqtt5Client.builder().identifier(nonShareSubClient).buildAsync();
    Mqtt5AsyncClient mqtt5PubAsyncClient = Mqtt5Client.builder().identifier("pubClient").buildAsync();
    String share1 = "share1";
    String share2 = "share2";
    mqtt5Share1SubClient1.connect()
      .thenCompose(v -> {
        Mqtt5Subscribe mqtt5Subscribe = Mqtt5Subscribe.builder()
          .topicFilter("$share/" + share1 + "/abc/def/123")
          .qos(MqttQos.AT_LEAST_ONCE)
          .build();
        return mqtt5Share1SubClient1.subscribe(mqtt5Subscribe, mqtt5Publish -> {
          int index = publishReceivedCounter1.incrementAndGet();
          String payload = new String(mqtt5Publish.getPayloadAsBytes(), StandardCharsets.UTF_8);
          LOGGER.info("PUBLISH received, clientId: {}, sharename: {}, index: {}, topic: {}, qos: {}, payload: {}", share1SubClient1, share1, index, mqtt5Publish.getTopic(), mqtt5Publish.getQos().getCode(), payload);
          if (index == 2) {
            checkpoint.flag();
          }
        });
      })
      .thenCompose(v -> mqtt5SubAsyncClient2.connect())
      .thenCompose(v -> {
        Mqtt5Subscribe mqtt5Subscribe = Mqtt5Subscribe.builder()
          .topicFilter("$share/" + share1 + "/abc/def/123")
          .qos(MqttQos.AT_LEAST_ONCE)
          .build();
        return mqtt5SubAsyncClient2.subscribe(mqtt5Subscribe, mqtt5Publish -> {
          int index = publishReceivedCounter2.incrementAndGet();
          String payload = new String(mqtt5Publish.getPayloadAsBytes(), StandardCharsets.UTF_8);
          LOGGER.info("PUBLISH received, clientId: {}, sharename: {}, index: {}, topic: {}, qos: {}, payload: {}", share1SubClient2, share1, index, mqtt5Publish.getTopic(), mqtt5Publish.getQos().getCode(), payload);
          if (index == 2) {
            checkpoint.flag();
          }
        });
      })
      .thenCompose(v -> mqtt5Share2SubClient1.connect())
      .thenCompose(v -> {
        Mqtt5Subscribe mqtt5Subscribe = Mqtt5Subscribe.builder()
          .topicFilter("$share/" + share2 + "/abc/def/123")
          .qos(MqttQos.AT_LEAST_ONCE)
          .build();
        return mqtt5Share2SubClient1.subscribe(mqtt5Subscribe, mqtt5Publish -> {
          int index = publishReceivedCounter3.incrementAndGet();
          String payload = new String(mqtt5Publish.getPayloadAsBytes(), StandardCharsets.UTF_8);
          LOGGER.info("PUBLISH received, clientId: {}, sharename: {}, index: {}, topic: {}, qos: {}, payload: {}", share2SubClient1, share2, index, mqtt5Publish.getTopic(), mqtt5Publish.getQos().getCode(), payload);
          if (index == 4) {
            checkpoint.flag();
          }
        });
      })
      .thenCompose(v -> mqtt5SubAsyncClient4.connect())
      .thenCompose(v -> {
        Mqtt5Subscribe mqtt5Subscribe = Mqtt5Subscribe.builder()
          .topicFilter("abc/def/123")
          .qos(MqttQos.AT_LEAST_ONCE)
          .build();
        return mqtt5SubAsyncClient4.subscribe(mqtt5Subscribe, mqtt5Publish -> {
          int index = publishReceivedCounter4.incrementAndGet();
          String payload = new String(mqtt5Publish.getPayloadAsBytes(), StandardCharsets.UTF_8);
          LOGGER.info("PUBLISH received, clientId: {}, index: {}, topic: {}, qos: {}, payload: {}", nonShareSubClient, index, mqtt5Publish.getTopic(), mqtt5Publish.getQos().getCode(), payload);
          if (index == 4) {
            checkpoint.flag();
          }
        });
      })
      .thenCompose(v -> mqtt5PubAsyncClient.connect())
      .thenCompose(mqtt5ConnAck -> {
        List<CompletableFuture<Mqtt5PublishResult>> futures = new ArrayList<>();
        for (int i = 1; i <= 4; i++) {
          String payload = "p" + i;
          futures.add(mqtt5PubAsyncClient.publish(Mqtt5Publish.builder().topic("abc/def/123").qos(MqttQos.AT_MOST_ONCE).payload(payload.getBytes(StandardCharsets.UTF_8)).build()));
        }
        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
      })
      .whenComplete((v, t) -> {
        if (t != null) {
          testContext.failNow(t);
        }
      });
  }

}
