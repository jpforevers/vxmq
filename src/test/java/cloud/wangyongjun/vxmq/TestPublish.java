package cloud.wangyongjun.vxmq;

import cloud.wangyongjun.vxmq.assist.Config;
import io.vertx.junit5.VertxTestContext;
import io.vertx.mutiny.core.Vertx;
import io.vertx.mutiny.core.buffer.Buffer;
import io.vertx.mutiny.core.net.NetClient;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestPublish extends BaseTest {

  private static final Logger LOGGER = LoggerFactory.getLogger(TestPublish.class);

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

}
