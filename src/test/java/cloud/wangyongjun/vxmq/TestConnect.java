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

import io.netty.handler.codec.mqtt.MqttConnectReturnCode;
import io.vertx.junit5.VertxTestContext;
import io.vertx.mqtt.MqttClientOptions;
import io.vertx.mutiny.core.Vertx;
import io.vertx.mutiny.mqtt.MqttClient;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestConnect extends BaseTest {

  private static final Logger LOGGER = LoggerFactory.getLogger(TestConnect.class);

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
  }

}
