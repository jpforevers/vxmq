package io.github.jpforevers.vxmq;

import com.hivemq.client.mqtt.datatypes.MqttQos;
import com.hivemq.client.mqtt.mqtt3.Mqtt3AsyncClient;
import com.hivemq.client.mqtt.mqtt3.Mqtt3Client;
import com.hivemq.client.mqtt.mqtt3.message.publish.Mqtt3Publish;
import com.hivemq.client.mqtt.mqtt3.message.subscribe.Mqtt3Subscribe;
import com.hivemq.client.mqtt.mqtt5.Mqtt5AsyncClient;
import com.hivemq.client.mqtt.mqtt5.Mqtt5Client;
import com.hivemq.client.mqtt.mqtt5.message.connect.Mqtt5Connect;
import com.hivemq.client.mqtt.mqtt5.message.connect.Mqtt5ConnectRestrictions;
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5Publish;
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5PublishResult;
import com.hivemq.client.mqtt.mqtt5.message.subscribe.Mqtt5Subscribe;
import io.vertx.junit5.VertxTestContext;
import io.vertx.mutiny.core.Vertx;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

public class TestOutboundReceptionMaximum extends BaseTest {

  private static final Logger LOGGER = LoggerFactory.getLogger(TestOutboundReceptionMaximum.class);

  @Test
  public void testMqtt311OutboundReceptionMaximum(Vertx vertx, VertxTestContext testContext) throws Throwable {
    int publishSendNum = 20;
    CountDownLatch countDownLatch = new CountDownLatch(publishSendNum);
    AtomicInteger publishReceivedCounter = new AtomicInteger(0);
    Mqtt3AsyncClient mqtt3SubAsyncClient = Mqtt3Client.builder().identifier("testMqtt311OutboundReceptionMaximumSubClient").buildAsync();
    Mqtt3AsyncClient mqtt3PubAsyncClient = Mqtt3Client.builder().identifier("testMqtt311OutboundReceptionMaximumPubClient").buildAsync();
    mqtt3SubAsyncClient.connect()
      .thenCompose(v -> mqtt3PubAsyncClient.connect())
      .thenCompose(v -> mqtt3SubAsyncClient.subscribe(Mqtt3Subscribe.builder().topicFilter("testMqtt311OutboundReceptionMaximum").qos(MqttQos.AT_LEAST_ONCE).build(), mqtt3Publish -> {
        LOGGER.info("PUBLISH received, topic: {}, qos: {}, payload: {}", mqtt3Publish.getTopic(), mqtt3Publish.getQos().getCode(), new String(mqtt3Publish.getPayloadAsBytes()));
        publishReceivedCounter.incrementAndGet();
      }, true))
      .thenCompose(v -> {
        List<CompletableFuture<Mqtt3Publish>> completableFutures = new ArrayList<>();
        for (int i = 1; i <= publishSendNum; i++) {
          CompletableFuture<Mqtt3Publish> completableFuture = mqtt3PubAsyncClient
            .publish(Mqtt3Publish.builder().topic("testMqtt311OutboundReceptionMaximum").qos(MqttQos.AT_LEAST_ONCE).payload(("p" + i).getBytes(StandardCharsets.UTF_8)).build())
            .thenApply(mqtt3Publish -> {
              countDownLatch.countDown();
              return mqtt3Publish;
            });
          completableFutures.add(completableFuture);
        }
        return CompletableFuture.allOf(completableFutures.toArray(CompletableFuture[]::new));
      });

    try {
      countDownLatch.await();
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
    Thread.sleep(3000);
    LOGGER.info("PUBLISH received count: {}", publishReceivedCounter.get());
    assertEquals(OUTBOUND_RECEIVE_MAXIMUM, publishReceivedCounter.get());
    testContext.completeNow();
  }

  @Test
  public void testMqtt5OutboundReceptionMaximum(Vertx vertx, VertxTestContext testContext) throws Throwable {
    int receiveMaximum = 5;
    int publishSendNum = 20;
    CountDownLatch countDownLatch = new CountDownLatch(publishSendNum);
    AtomicInteger publishReceivedCounter = new AtomicInteger(0);
    Mqtt5AsyncClient mqtt5SubAsyncClient = Mqtt5Client.builder().identifier("testMqtt5OutboundReceptionMaximumSubClient").buildAsync();
    Mqtt5AsyncClient mqtt5PubAsyncClient = Mqtt5Client.builder().identifier("testMqtt5OutboundReceptionMaximumPubClient").buildAsync();
    mqtt5SubAsyncClient.connect(Mqtt5Connect.builder().restrictions(Mqtt5ConnectRestrictions.builder().receiveMaximum(receiveMaximum).build()).build())
      .thenCompose(v -> mqtt5PubAsyncClient.connect())
      .thenCompose(v -> mqtt5SubAsyncClient.subscribe(Mqtt5Subscribe.builder().topicFilter("testMqtt5OutboundReceptionMaximum").qos(MqttQos.AT_LEAST_ONCE).build(), mqtt5Publish -> {
        LOGGER.info("PUBLISH received, topic: {}, qos: {}, payload: {}", mqtt5Publish.getTopic(), mqtt5Publish.getQos().getCode(), new String(mqtt5Publish.getPayloadAsBytes()));
        publishReceivedCounter.incrementAndGet();
      }, true))
      .thenCompose(v -> {
        List<CompletableFuture<Mqtt5PublishResult>> completableFutures = new ArrayList<>();
        for (int i = 1; i <= publishSendNum; i++) {
          CompletableFuture<Mqtt5PublishResult> completableFuture = mqtt5PubAsyncClient
            .publish(Mqtt5Publish.builder().topic("testMqtt5OutboundReceptionMaximum").qos(MqttQos.AT_LEAST_ONCE).payload(("p" + i).getBytes(StandardCharsets.UTF_8)).build())
            .thenApply(mqtt5PublishResult -> {
              countDownLatch.countDown();
              return mqtt5PublishResult;
            });
          completableFutures.add(completableFuture);
        }
        return CompletableFuture.allOf(completableFutures.toArray(CompletableFuture[]::new));
      });

    try {
      countDownLatch.await();
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
    Thread.sleep(3000);
    LOGGER.info("PUBLISH received count: {}", publishReceivedCounter.get());
    assertEquals(receiveMaximum, publishReceivedCounter.get());
    testContext.completeNow();
  }

}
