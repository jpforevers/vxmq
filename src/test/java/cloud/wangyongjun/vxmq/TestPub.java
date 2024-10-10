package cloud.wangyongjun.vxmq;

import cloud.wangyongjun.vxmq.assist.Config;
import com.hivemq.client.mqtt.datatypes.MqttQos;
import com.hivemq.client.mqtt.mqtt5.Mqtt5AsyncClient;
import com.hivemq.client.mqtt.mqtt5.Mqtt5Client;
import com.hivemq.client.mqtt.mqtt5.message.connect.Mqtt5Connect;
import com.hivemq.client.mqtt.mqtt5.message.connect.Mqtt5ConnectRestrictions;
import com.hivemq.client.mqtt.mqtt5.message.connect.connack.Mqtt5ConnAckReasonCode;
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5Publish;
import com.hivemq.client.mqtt.mqtt5.message.subscribe.Mqtt5Subscribe;
import com.hivemq.client.mqtt.mqtt5.message.subscribe.suback.Mqtt5SubAckReasonCode;
import io.vertx.junit5.VertxTestContext;
import io.vertx.mutiny.core.Vertx;
import io.vertx.mutiny.core.buffer.Buffer;
import io.vertx.mutiny.core.net.NetClient;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestPub extends BaseTest {

  private static final Logger LOGGER = LoggerFactory.getLogger(TestPub.class);

  @Test
  public void testMqtt5PubTopicAliasMax(Vertx vertx, VertxTestContext testContext) throws Throwable {
    System.setProperty(Config.KEY_VXMQ_MQTT_TOPIC_ALIAS_MAX, "3");
    NetClient netClient = vertx.createNetClient();
    netClient.connect(Config.getMqttServerPort(), "localhost")
      .onItem().invoke(so -> so.handler(buffer -> {
        if (0xe0 == buffer.getUnsignedByte(0)) {  // DISCONNECT
          assertEquals(0x94, buffer.getUnsignedByte(buffer.length() - 2));  // Topic alias invalid
          testContext.completeNow();
        }
      }))
      .onItem().call(so -> {
        Buffer buffer = Buffer.buffer();
        buffer.appendUnsignedByte((short) 0b00010000);  // Fixed header byte 1
        buffer.appendUnsignedByte((short) 0b00000000);  // Fixed header byte 2, Remaining Length, update later

        buffer.appendUnsignedByte((short) 0b00000000);  // Variable header byte 1, Protocol Name Length MSB (0)
        buffer.appendUnsignedByte((short) 0b00000100);  // Variable header byte 2, Protocol Name Length LSB (4)
        buffer.appendUnsignedByte((short) 0b01001101);  // Variable header byte 3, Protocol Name M
        buffer.appendUnsignedByte((short) 0b01010001);  // Variable header byte 4, Protocol Name Q
        buffer.appendUnsignedByte((short) 0b01010100);  // Variable header byte 5, Protocol Name T
        buffer.appendUnsignedByte((short) 0b01010100);  // Variable header byte 6, Protocol Name T
        buffer.appendUnsignedByte((short) 0b00000101);  // Variable header byte 7, Protocol Level
        buffer.appendUnsignedByte((short) 0b11000010);  // Variable header byte 8, Connect Flags, username, password and clean session set to 1
        buffer.appendBytes(encodeToMqttTwoByteIntegerBytes(30));  // Variable header, Keep Alive
        Buffer properties = Buffer.buffer();
        properties.appendUnsignedByte((short) 0b00000000);
        properties.setBytes(0, encodeVariableByteIntegerBytes(properties.length() - 1));
        buffer.appendBuffer(properties);  // Variable header, Properties

        buffer.appendBytes(encodeToMqttUtf8EncodedStringBytes("clientId1"));  // Payload, client id
        buffer.appendBytes(encodeToMqttUtf8EncodedStringBytes("username1")); // Payload, username
        buffer.appendBytes(encodeToMqttPasswordBytes("password".getBytes(StandardCharsets.UTF_8)));  // Payload, password

        buffer.setBytes(1, encodeVariableByteIntegerBytes(buffer.length() - 2));  // Update remaining length

        return so.write(buffer);
      })
      .onItem().delayIt().by(Duration.ofSeconds(1))
      .onItem().call(so -> {
        Buffer buffer = Buffer.buffer();
        buffer.appendUnsignedByte((short) 0b00110010);  // Fixed header byte 1
        buffer.appendUnsignedByte((short) 0b00000000);  // Fixed header byte 2, Remaining Length, update later

        buffer.appendBytes(encodeToMqttUtf8EncodedStringBytes("topic"));  // Variable header, Topic Name
        buffer.appendBytes(encodeToMqttTwoByteIntegerBytes(1));  // Variable header, Packet Identifier

        Buffer properties = Buffer.buffer();
        properties.appendUnsignedByte((short) 0b00000000);
        properties.appendUnsignedByte((short) 0x23);
        properties.appendBytes(encodeToMqttTwoByteIntegerBytes(4));
        properties.setBytes(0, encodeVariableByteIntegerBytes(properties.length() - 1));
        buffer.appendBuffer(properties);  // Variable header, Properties

        buffer.appendBytes("p1".getBytes(StandardCharsets.UTF_8));

        buffer.setBytes(1, encodeVariableByteIntegerBytes(buffer.length() - 2));  // Update remaining length

        return so.write(buffer);
      })
      .subscribe().with(v -> {}, testContext::failNow);
  }

  @Test
  public void testMqtt5PubInboundTopicAlias(Vertx vertx, VertxTestContext testContext) throws Throwable {
    AtomicInteger atomicInteger = new AtomicInteger();
    Mqtt5AsyncClient mqtt5SubAsyncClient = Mqtt5Client.builder()
      .identifier("subClient")
      .buildAsync();
    Mqtt5AsyncClient mqtt5PubAsyncClient = Mqtt5Client.builder()
      .identifier("pubClient")
      .buildAsync();
    mqtt5SubAsyncClient.connect()
      .thenAccept(mqtt5ConnAck -> assertEquals(Mqtt5ConnAckReasonCode.SUCCESS, mqtt5ConnAck.getReasonCode()))
      .thenCompose(v -> {
        Mqtt5Subscribe mqtt5Subscribe = Mqtt5Subscribe.builder()
          .topicFilter("abc/def/+")
          .qos(MqttQos.AT_LEAST_ONCE)
          .build();
        return mqtt5SubAsyncClient.subscribe(mqtt5Subscribe, mqtt5Publish -> {
          int index = atomicInteger.addAndGet(1);
          assertTrue(mqtt5Publish.getPayload().isPresent());
          if (index == 1) {
            assertEquals("p1", Charset.defaultCharset().decode(mqtt5Publish.getPayload().get()).toString());
          }
          if (index == 2) {
            assertEquals("p2", Charset.defaultCharset().decode(mqtt5Publish.getPayload().get()).toString());
            testContext.completeNow();
          }
        });
      })
      .thenAccept(mqtt5SubAck -> mqtt5SubAck.getReasonCodes().forEach(mqtt5SubAckReasonCode -> assertEquals(Mqtt5SubAckReasonCode.GRANTED_QOS_1, mqtt5SubAckReasonCode)))
      .thenCompose(v -> mqtt5PubAsyncClient.connect())
      .thenAccept(mqtt5ConnAck -> assertEquals(Mqtt5ConnAckReasonCode.SUCCESS, mqtt5ConnAck.getReasonCode()))
      // First PUBLISH, hivemq client will carry topic and alias
      .thenCompose(v -> mqtt5PubAsyncClient.publish(Mqtt5Publish.builder().topic("abc/def/123").qos(MqttQos.AT_LEAST_ONCE).payload("p1".getBytes(StandardCharsets.UTF_8)).build()))
      .thenAccept(mqtt5PublishResult -> assertTrue(mqtt5PublishResult.getError().isEmpty()))
      // Second PUBLISH with same topic, hivemq client will only carry alias
      .thenCompose(v -> mqtt5PubAsyncClient.publish(Mqtt5Publish.builder().topic("abc/def/123").qos(MqttQos.AT_LEAST_ONCE).payload("p2".getBytes(StandardCharsets.UTF_8)).build()))
      .thenAccept(mqtt5PublishResult -> assertTrue(mqtt5PublishResult.getError().isEmpty()))
      .whenComplete((v, t) -> {
        if (t != null) {
          testContext.failNow(t);
        }
      });
  }

  @Test
  public void testMqtt5PubOutboundTopicAlias(Vertx vertx, VertxTestContext testContext) throws Throwable {
    AtomicInteger atomicInteger = new AtomicInteger();
    Mqtt5AsyncClient mqtt5SubAsyncClient = Mqtt5Client.builder()
      .identifier("subClient")
      .buildAsync();
    Mqtt5AsyncClient mqtt5PubAsyncClient = Mqtt5Client.builder()
      .identifier("pubClient")
      .buildAsync();
    mqtt5SubAsyncClient.connect(Mqtt5Connect.builder()
        .restrictions(Mqtt5ConnectRestrictions.builder().topicAliasMaximum(1).build())
        .build())
      .thenAccept(mqtt5ConnAck -> assertEquals(Mqtt5ConnAckReasonCode.SUCCESS, mqtt5ConnAck.getReasonCode()))
      .thenCompose(v -> {
        Mqtt5Subscribe mqtt5Subscribe = Mqtt5Subscribe.builder()
          .topicFilter("abc/def/+")
          .qos(MqttQos.AT_LEAST_ONCE)
          .build();
        return mqtt5SubAsyncClient.subscribe(mqtt5Subscribe, mqtt5Publish -> {
          int index = atomicInteger.addAndGet(1);
          assertTrue(mqtt5Publish.getPayload().isPresent());
          if (index == 1) {
            assertEquals("p1", Charset.defaultCharset().decode(mqtt5Publish.getPayload().get()).toString());
          }
          if (index == 2) {
            assertEquals("p2", Charset.defaultCharset().decode(mqtt5Publish.getPayload().get()).toString());
            testContext.completeNow();
          }
        });
      })
      .thenAccept(mqtt5SubAck -> mqtt5SubAck.getReasonCodes().forEach(mqtt5SubAckReasonCode -> assertEquals(Mqtt5SubAckReasonCode.GRANTED_QOS_1, mqtt5SubAckReasonCode)))
      .thenCompose(v -> mqtt5PubAsyncClient.connect())
      .thenAccept(mqtt5ConnAck -> assertEquals(Mqtt5ConnAckReasonCode.SUCCESS, mqtt5ConnAck.getReasonCode()))
      .thenCompose(v -> mqtt5PubAsyncClient.publish(Mqtt5Publish.builder().topic("abc/def/123").qos(MqttQos.AT_LEAST_ONCE).payload("p1".getBytes(StandardCharsets.UTF_8)).build()))
      .thenAccept(mqtt5PublishResult -> assertTrue(mqtt5PublishResult.getError().isEmpty()))
      .thenCompose(v -> mqtt5PubAsyncClient.publish(Mqtt5Publish.builder().topic("abc/def/123").qos(MqttQos.AT_LEAST_ONCE).payload("p2".getBytes(StandardCharsets.UTF_8)).build()))
      .thenAccept(mqtt5PublishResult -> assertTrue(mqtt5PublishResult.getError().isEmpty()))
      .whenComplete((v, t) -> {
        if (t != null) {
          testContext.failNow(t);
        }
      });
  }

}
