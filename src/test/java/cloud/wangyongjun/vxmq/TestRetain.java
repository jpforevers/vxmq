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
 *
 */

package cloud.wangyongjun.vxmq;

import com.hivemq.client.mqtt.datatypes.MqttQos;
import com.hivemq.client.mqtt.mqtt3.Mqtt3AsyncClient;
import com.hivemq.client.mqtt.mqtt3.Mqtt3Client;
import com.hivemq.client.mqtt.mqtt3.message.publish.Mqtt3Publish;
import com.hivemq.client.mqtt.mqtt3.message.subscribe.Mqtt3Subscribe;
import com.hivemq.client.mqtt.mqtt3.message.unsubscribe.Mqtt3Unsubscribe;
import io.vertx.junit5.VertxTestContext;
import io.vertx.mutiny.core.Vertx;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestRetain extends BaseTest{

  @Test
  void testMqtt311Retain(Vertx vertx, VertxTestContext testContext) throws Throwable {
    String topicFilter = "test/retain/+";
    String topicName = "test/retain/abc";
    String retainPayload1 = "retainPayload1";
    String retainPayload2 = "retainPayload2";
    Mqtt3AsyncClient mqtt3AsyncClient1 = Mqtt3Client.builder()
      .identifier("testMqtt311Retain1")
      .buildAsync();
    Mqtt3AsyncClient mqtt3AsyncClient2 = Mqtt3Client.builder()
      .identifier("testMqtt311Retain2")
      .buildAsync();
    mqtt3AsyncClient1.connect()
      .thenCompose(v -> mqtt3AsyncClient2.connect())
      .thenCompose(v -> {
        Mqtt3Publish mqtt3Publish = Mqtt3Publish.builder().topic(topicName).qos(MqttQos.AT_LEAST_ONCE).retain(true)
          .payload(retainPayload1.getBytes(StandardCharsets.UTF_8)).build();
        return mqtt3AsyncClient2.publish(mqtt3Publish);
      })
      .thenCompose(v -> {
        Mqtt3Subscribe mqtt3Subscribe = Mqtt3Subscribe.builder().topicFilter(topicFilter).qos(MqttQos.AT_LEAST_ONCE).build();
        return mqtt3AsyncClient1.subscribe(mqtt3Subscribe, mqtt3Publish -> {
          assertTrue(mqtt3Publish.isRetain());
          assertEquals(retainPayload1, new String(mqtt3Publish.getPayloadAsBytes()));
        });
      })
      .thenCompose(v -> {
        Mqtt3Unsubscribe mqtt3Unsubscribe = Mqtt3Unsubscribe.builder().topicFilter(topicFilter).build();
        return mqtt3AsyncClient1.unsubscribe(mqtt3Unsubscribe);
      })
      .thenCompose(v -> {
        Mqtt3Publish mqtt3Publish = Mqtt3Publish.builder().topic(topicName).qos(MqttQos.AT_LEAST_ONCE).retain(true)
          .payload(retainPayload2.getBytes(StandardCharsets.UTF_8)).build();
        return mqtt3AsyncClient2.publish(mqtt3Publish);
      })
      .thenCompose(v -> {
        Mqtt3Subscribe mqtt3Subscribe = Mqtt3Subscribe.builder().topicFilter(topicFilter).qos(MqttQos.AT_LEAST_ONCE).build();
        return mqtt3AsyncClient1.subscribe(mqtt3Subscribe, mqtt3Publish -> {
          assertTrue(mqtt3Publish.isRetain());
          assertEquals(retainPayload2, new String(mqtt3Publish.getPayloadAsBytes()));
          testContext.completeNow();
        });
      })
      .whenComplete((v, t) -> {
        if (t != null) {
          testContext.failNow(t);
        }
      });
  }

}
