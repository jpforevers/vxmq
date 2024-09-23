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

import cloud.wangyongjun.vxmq.assist.MqttPropertiesUtil;
import io.netty.handler.codec.mqtt.MqttProperties;
import io.vertx.core.json.JsonArray;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import io.vertx.mutiny.core.Vertx;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(VertxExtension.class)
public class TestMqttPropertiesUtil {

  private static final MqttProperties MQTT_PROPERTIES = new MqttProperties();
  private static final String MQTT_PROPERTIES_JSON = "[{\"id\":41,\"value\":1,\"type\":\"IntegerProperty\"},{\"id\":9,\"value\":\"eHh4\",\"type\":\"BinaryProperty\"},{\"id\":3,\"value\":\"application/json\",\"type\":\"StringProperty\"},{\"id\":11,\"value\":11,\"type\":\"IntegerProperty\"},{\"id\":11,\"value\":12,\"type\":\"IntegerProperty\"},{\"id\":38,\"key\":\"k1\",\"value\":\"v1\",\"type\":\"UserProperty\"},{\"id\":38,\"key\":\"k2\",\"value\":\"v2\",\"type\":\"UserProperty\"}]";

  @BeforeAll
  public static void beforeAll() {
    MqttProperties.IntegerProperty integerProperty = new MqttProperties.IntegerProperty(MqttProperties.MqttPropertyType.SUBSCRIPTION_IDENTIFIER_AVAILABLE.value(), 1);
    MqttProperties.IntegerProperty subscriptionIdentifierProperty1 = new MqttProperties.IntegerProperty(MqttProperties.MqttPropertyType.SUBSCRIPTION_IDENTIFIER.value(), 11);
    MqttProperties.IntegerProperty subscriptionIdentifierProperty2 = new MqttProperties.IntegerProperty(MqttProperties.MqttPropertyType.SUBSCRIPTION_IDENTIFIER.value(), 12);
    MqttProperties.StringProperty stringProperty = new MqttProperties.StringProperty(MqttProperties.MqttPropertyType.CONTENT_TYPE.value(), "application/json");
    MqttProperties.BinaryProperty binaryProperty = new MqttProperties.BinaryProperty(MqttProperties.MqttPropertyType.CORRELATION_DATA.value(), "xxx".getBytes(StandardCharsets.UTF_8));
    MqttProperties.UserProperty userProperty1 = new MqttProperties.UserProperty("k1", "v1");
    MqttProperties.UserProperty userProperty2 = new MqttProperties.UserProperty("k2", "v2");

    MQTT_PROPERTIES.add(integerProperty);
    MQTT_PROPERTIES.add(subscriptionIdentifierProperty1);
    MQTT_PROPERTIES.add(subscriptionIdentifierProperty2);
    MQTT_PROPERTIES.add(stringProperty);
    MQTT_PROPERTIES.add(binaryProperty);
    MQTT_PROPERTIES.add(userProperty1);
    MQTT_PROPERTIES.add(userProperty2);
  }

  @Test
  public void testEncode(Vertx vertx, VertxTestContext testContext) {
    JsonArray jsonArray = MqttPropertiesUtil.encode(MQTT_PROPERTIES);
    System.out.println(jsonArray);
    assertEquals(MQTT_PROPERTIES_JSON, jsonArray.toString());
    testContext.completeNow();
  }

  @Test
  public void testDecode(Vertx vertx, VertxTestContext testContext) {
    MqttProperties mqttProperties = MqttPropertiesUtil.decode(new JsonArray(MQTT_PROPERTIES_JSON));
    assertEquals(MQTT_PROPERTIES_JSON, MqttPropertiesUtil.encode(mqttProperties).toString());
    testContext.completeNow();
  }

  @Test
  public void testGetValue(Vertx vertx, VertxTestContext testContext) {
    Integer integer = MqttPropertiesUtil.getValue(MQTT_PROPERTIES, MqttProperties.MqttPropertyType.SUBSCRIPTION_IDENTIFIER_AVAILABLE, MqttProperties.IntegerProperty.class);
    assertEquals(1, integer);

    List<Integer> integers = MqttPropertiesUtil.getValues(MQTT_PROPERTIES, MqttProperties.MqttPropertyType.SUBSCRIPTION_IDENTIFIER, MqttProperties.IntegerProperty.class);
    assertEquals(List.of(11, 12), integers);

    String string = MqttPropertiesUtil.getValue(MQTT_PROPERTIES, MqttProperties.MqttPropertyType.CONTENT_TYPE, MqttProperties.StringProperty.class);
    assertEquals("application/json", string);

    byte[] bytes = MqttPropertiesUtil.getValue(MQTT_PROPERTIES, MqttProperties.MqttPropertyType.CORRELATION_DATA, MqttProperties.BinaryProperty.class);
    assertArrayEquals("xxx".getBytes(StandardCharsets.UTF_8), bytes);

    List<MqttProperties.StringPair> stringPairs = MqttPropertiesUtil.getValues(MQTT_PROPERTIES, MqttProperties.MqttPropertyType.USER_PROPERTY, MqttProperties.UserProperty.class);
    assertEquals(List.of(new MqttProperties.StringPair("k1", "v1"), new MqttProperties.StringPair("k2", "v2")), stringPairs);

    testContext.completeNow();
  }

}
