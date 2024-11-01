/*
 * Copyright (C) 2023-2024 王用军
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package io.github.jpforevers.vxmq;

import io.github.jpforevers.vxmq.assist.Config;
import com.hivemq.client.mqtt.lifecycle.MqttDisconnectSource;
import com.hivemq.client.mqtt.mqtt3.Mqtt3AsyncClient;
import com.hivemq.client.mqtt.mqtt3.Mqtt3Client;
import com.hivemq.client.mqtt.mqtt3.message.connect.connack.Mqtt3ConnAckReturnCode;
import com.hivemq.client.mqtt.mqtt5.Mqtt5AsyncClient;
import com.hivemq.client.mqtt.mqtt5.Mqtt5Client;
import com.hivemq.client.mqtt.mqtt5.exceptions.Mqtt5ConnAckException;
import com.hivemq.client.mqtt.mqtt5.exceptions.Mqtt5DisconnectException;
import com.hivemq.client.mqtt.mqtt5.message.connect.connack.Mqtt5ConnAckReasonCode;
import com.hivemq.client.mqtt.mqtt5.message.disconnect.Mqtt5DisconnectReasonCode;
import io.vertx.junit5.VertxTestContext;
import io.vertx.mutiny.core.Vertx;
import io.vertx.mutiny.core.buffer.Buffer;
import io.vertx.mutiny.core.net.NetClient;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;

public class TestConnect extends BaseTest {

  private static final Logger LOGGER = LoggerFactory.getLogger(TestConnect.class);

  @Test
  void testMqtt311ConnSuccess(Vertx vertx, VertxTestContext testContext) throws Throwable {
    Mqtt3AsyncClient mqtt3AsyncClient = Mqtt3Client.builder()
      .identifier("clientId1")
      .buildAsync();
    mqtt3AsyncClient.connect()
      .thenAccept(mqtt3ConnAck -> assertEquals(Mqtt3ConnAckReturnCode.SUCCESS, mqtt3ConnAck.getReturnCode()))
      .thenCompose(v -> mqtt3AsyncClient.disconnect())
      .whenComplete(whenCompleteBiConsumer(testContext));
  }

  @Test
  void testMqttConnUnsupportedMqttVersion(Vertx vertx, VertxTestContext testContext) throws Throwable {
    NetClient netClient = vertx.createNetClient();
    netClient.connect(Config.getMqttServerPort(), "localhost")
      .onItem().invoke(so -> so.handler(buffer -> {
        assertEquals(Mqtt3ConnAckReturnCode.UNSUPPORTED_PROTOCOL_VERSION.getCode(), buffer.getByte(buffer.length() - 1));
        testContext.completeNow();
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
        buffer.appendUnsignedByte((short) 0b00000110);  // Variable header byte 7, Protocol Level, set wrong value 6
        buffer.appendUnsignedByte((short) 0b11000010);  // Variable header byte 8, Connect Flags, username, password and clean session set to 1

        buffer.appendBytes(encodeToMqttTwoByteIntegerBytes(30));  // Variable header, Keep Alive

        buffer.appendBytes(encodeToMqttUtf8EncodedStringBytes("clientId1"));  // Payload, client id
        buffer.appendBytes(encodeToMqttUtf8EncodedStringBytes("username1")); // Payload, username
        buffer.appendBytes(encodeToMqttPasswordBytes("password".getBytes(StandardCharsets.UTF_8)));  // Payload, password

        buffer.setBytes(1, encodeVariableByteIntegerBytes(buffer.length() - 2));  // Update remaining length

        return so.write(buffer);
      })
      .subscribe().with(v -> {}, testContext::failNow);
  }

  @Test
  void testMqtt5ConnSuccess(Vertx vertx, VertxTestContext testContext) throws Throwable {
    Mqtt5AsyncClient mqtt5AsyncClient = Mqtt5Client.builder()
      .identifier("clientId1")
      .buildAsync();
    mqtt5AsyncClient.connect()
      .thenAccept(mqtt5ConnAck -> assertEquals(Mqtt5ConnAckReasonCode.SUCCESS, mqtt5ConnAck.getReasonCode()))
      .thenCompose(v -> mqtt5AsyncClient.disconnect())
      .whenComplete(whenCompleteBiConsumer(testContext));
  }

  @Test
  void testMqtt5ConnWithoutClientId(Vertx vertx, VertxTestContext testContext) throws Throwable {
    Mqtt5AsyncClient mqtt5AsyncClient = Mqtt5Client.builder()
      .buildAsync();
    mqtt5AsyncClient.connect()
      .thenAccept(mqtt5ConnAck -> {
        assertEquals(Mqtt5ConnAckReasonCode.SUCCESS, mqtt5ConnAck.getReasonCode());
        assertTrue(mqtt5ConnAck.getAssignedClientIdentifier().isPresent(), "Mqtt 5 property assigned client identifier not set");
      })
      .thenCompose(v -> mqtt5AsyncClient.disconnect())
      .whenComplete(whenCompleteBiConsumer(testContext));
  }

  @Test
  void testMqtt5ConnClientIdTooLong(Vertx vertx, VertxTestContext testContext) throws Throwable {
    Mqtt5AsyncClient mqtt5AsyncClient = Mqtt5Client.builder()
      .identifier("a".repeat(Config.getMqttClientIdLengthMax() + 1))
      .buildAsync();
    mqtt5AsyncClient.connect()
      .handle((mqtt5ConnAck, throwable) -> {
        assertNotNull(throwable);
        assertInstanceOf(Mqtt5ConnAckException.class, throwable);
        assertEquals(Mqtt5ConnAckReasonCode.CLIENT_IDENTIFIER_NOT_VALID, ((Mqtt5ConnAckException) throwable).getMqttMessage().getReasonCode());
        assertTrue(((Mqtt5ConnAckException) throwable).getMqttMessage().getReasonString().isPresent());
        return null;
      })
      .whenComplete(whenCompleteBiConsumer(testContext));
  }

  @Test
  void testMqtt5ConnSessionTakenOver(Vertx vertx, VertxTestContext testContext) throws Throwable {
    CompletableFuture<Void> disconnectFuture = new CompletableFuture<>();
    Mqtt5AsyncClient mqtt5AsyncClient = Mqtt5Client.builder()
      .addDisconnectedListener(mqttClientDisconnectedContext -> {
        try {
          assertEquals(MqttDisconnectSource.SERVER, mqttClientDisconnectedContext.getSource());
          assertInstanceOf(Mqtt5DisconnectException.class, mqttClientDisconnectedContext.getCause());
          assertEquals(Mqtt5DisconnectReasonCode.SESSION_TAKEN_OVER, ((Mqtt5DisconnectException) mqttClientDisconnectedContext.getCause()).getMqttMessage().getReasonCode());
          disconnectFuture.complete(null);
        } catch (Throwable t) {
          disconnectFuture.completeExceptionally(t);
        }
      })
      .identifier("c1")
      .buildAsync();
    Mqtt5AsyncClient mqtt5AsyncClientCopy = Mqtt5Client.builder()
      .identifier("c1")
      .buildAsync();
    mqtt5AsyncClient.connect()
      .thenCompose(mqtt5ConnAck -> {
        assertEquals(Mqtt5ConnAckReasonCode.SUCCESS, mqtt5ConnAck.getReasonCode());
        return mqtt5AsyncClientCopy.connect();
      })
      .thenAccept(mqtt5ConnAck -> assertEquals(Mqtt5ConnAckReasonCode.SUCCESS, mqtt5ConnAck.getReasonCode()))
      .thenCompose(v -> disconnectFuture)
      .whenComplete(whenCompleteBiConsumer(testContext));
  }

}
