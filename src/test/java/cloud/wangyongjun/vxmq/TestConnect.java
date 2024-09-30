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

package cloud.wangyongjun.vxmq;

import cloud.wangyongjun.vxmq.assist.Config;
import com.hivemq.client.mqtt.mqtt3.Mqtt3AsyncClient;
import com.hivemq.client.mqtt.mqtt3.Mqtt3Client;
import com.hivemq.client.mqtt.mqtt3.message.connect.connack.Mqtt3ConnAckReturnCode;
import com.hivemq.client.mqtt.mqtt5.Mqtt5AsyncClient;
import com.hivemq.client.mqtt.mqtt5.Mqtt5Client;
import com.hivemq.client.mqtt.mqtt5.exceptions.Mqtt5ConnAckException;
import com.hivemq.client.mqtt.mqtt5.message.connect.connack.Mqtt5ConnAckReasonCode;
import io.vertx.junit5.VertxTestContext;
import io.vertx.mutiny.core.Vertx;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.jupiter.api.Assertions.*;

public class TestConnect extends BaseTest {

  private static final Logger LOGGER = LoggerFactory.getLogger(TestConnect.class);

  @Test
  void testMqttSpec311Connect(Vertx vertx, VertxTestContext testContext) throws Throwable {
    Mqtt3AsyncClient mqtt3AsyncClient = Mqtt3Client.builder()
      .identifier("clientId1")
      .buildAsync();
    mqtt3AsyncClient.connect()
      .thenAccept(mqtt3ConnAck -> {
        assertEquals(Mqtt3ConnAckReturnCode.SUCCESS, mqtt3ConnAck.getReturnCode());
      })
      .thenCompose(v -> mqtt3AsyncClient.disconnect())
      .whenComplete(whenCompleteBiConsumer(testContext));
  }

  @Test
  void testMqttSpec5Connect(Vertx vertx, VertxTestContext testContext) throws Throwable {
    Mqtt5AsyncClient mqtt5AsyncClient = Mqtt5Client.builder()
      .identifier("clientId1")
      .buildAsync();
    mqtt5AsyncClient.connect()
      .thenAccept(mqtt5ConnAck -> {
        assertEquals(Mqtt5ConnAckReasonCode.SUCCESS, mqtt5ConnAck.getReasonCode());
      })
      .thenCompose(v -> mqtt5AsyncClient.disconnect())
      .whenComplete(whenCompleteBiConsumer(testContext));
  }

  @Test
  void testMqttSpec5WithoutClientId(Vertx vertx, VertxTestContext testContext) throws Throwable {
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
  void testMqttSpec5ClientIdTooLong(Vertx vertx, VertxTestContext testContext) throws Throwable {
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

}
