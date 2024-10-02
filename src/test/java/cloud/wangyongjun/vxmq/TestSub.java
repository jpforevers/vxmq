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
import cloud.wangyongjun.vxmq.service.sub.Subscription;
import com.hivemq.client.mqtt.datatypes.MqttQos;
import com.hivemq.client.mqtt.mqtt3.Mqtt3AsyncClient;
import com.hivemq.client.mqtt.mqtt3.Mqtt3Client;
import com.hivemq.client.mqtt.mqtt3.message.connect.connack.Mqtt3ConnAckReturnCode;
import com.hivemq.client.mqtt.mqtt3.message.subscribe.Mqtt3Subscribe;
import com.hivemq.client.mqtt.mqtt3.message.subscribe.Mqtt3Subscription;
import com.hivemq.client.mqtt.mqtt3.message.subscribe.suback.Mqtt3SubAckReturnCode;
import com.hivemq.client.mqtt.mqtt5.Mqtt5AsyncClient;
import com.hivemq.client.mqtt.mqtt5.Mqtt5Client;
import com.hivemq.client.mqtt.mqtt5.exceptions.Mqtt5SubAckException;
import com.hivemq.client.mqtt.mqtt5.message.connect.connack.Mqtt5ConnAckReasonCode;
import com.hivemq.client.mqtt.mqtt5.message.subscribe.Mqtt5Subscribe;
import com.hivemq.client.mqtt.mqtt5.message.subscribe.Mqtt5Subscription;
import com.hivemq.client.mqtt.mqtt5.message.subscribe.suback.Mqtt5SubAckReasonCode;
import io.vertx.junit5.VertxTestContext;
import io.vertx.mutiny.core.Vertx;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class TestSub extends BaseTest {

  private static final Logger LOGGER = LoggerFactory.getLogger(TestSub.class);

  @Test
  void testMqtt311Sub(Vertx vertx, VertxTestContext testContext) throws Throwable {
    Map<String, Integer> topicToQosMap = new HashMap<>();
    for (Subscription subscription : TestConstants.SUBSCRIPTIONS) {
      topicToQosMap.put(subscription.getTopicFilter(), subscription.getQos());
    }

    Mqtt3AsyncClient mqtt3AsyncClient = Mqtt3Client.builder().buildAsync();
    mqtt3AsyncClient.connect()
      .thenAccept(mqtt3ConnAck -> assertEquals(Mqtt3ConnAckReturnCode.SUCCESS, mqtt3ConnAck.getReturnCode()))
      .thenCompose(v -> {
        List<Mqtt3Subscription> mqtt3Subscriptions = new ArrayList<>();
        for (Map.Entry<String, Integer> stringIntegerEntry : topicToQosMap.entrySet()) {
          Mqtt3Subscription mqtt3Subscription = Mqtt3Subscription.builder().topicFilter(stringIntegerEntry.getKey()).qos(MqttQos.fromCode(stringIntegerEntry.getValue())).build();
          mqtt3Subscriptions.add(mqtt3Subscription);
        }
        return mqtt3AsyncClient.subscribe(Mqtt3Subscribe.builder().addSubscriptions(mqtt3Subscriptions).build());
      })
      .thenAccept(mqtt3SubAck -> {
        mqtt3SubAck.getReturnCodes().forEach(mqtt3SubAckReturnCode -> assertNotEquals(Mqtt3SubAckReturnCode.FAILURE, mqtt3SubAckReturnCode));
      })
      .thenCompose(v -> mqtt3AsyncClient.disconnect())
      .whenComplete(whenCompleteBiConsumer(testContext));
  }

  @Test
  void testMqtt5SubTopicLevelsMax(Vertx vertx, VertxTestContext testContext) throws Throwable {
    Mqtt5AsyncClient mqtt5AsyncClient = Mqtt5Client.builder().buildAsync();
    mqtt5AsyncClient.connect()
      .thenAccept(mqtt5ConnAck -> assertEquals(Mqtt5ConnAckReasonCode.SUCCESS, mqtt5ConnAck.getReasonCode()))
      .thenCompose(v -> {
        int topicLevelsMax = Config.getMqttTopicLevelsMax();
        String topicFilterExceedingMax = 'a' + "/a".repeat(topicLevelsMax);
        Mqtt5Subscription mqtt5Subscription = Mqtt5Subscription.builder()
          .topicFilter(topicFilterExceedingMax)
          .qos(MqttQos.AT_MOST_ONCE)
          .build();
        return mqtt5AsyncClient.subscribe(Mqtt5Subscribe.builder().addSubscription(mqtt5Subscription).build());
      })
      .handle((mqtt5SubAck, t) -> {
        assertNotNull(t);
        assertInstanceOf(Mqtt5SubAckException.class, t.getCause());
        ((Mqtt5SubAckException) t.getCause()).getMqttMessage().getReasonCodes().forEach(mqtt5SubAckReasonCode -> assertEquals(Mqtt5SubAckReasonCode.TOPIC_FILTER_INVALID, mqtt5SubAckReasonCode));
        return null;
      })
      .thenCompose(v -> mqtt5AsyncClient.disconnect())
      .whenComplete(whenCompleteBiConsumer(testContext));
  }

}
