/*
 * Copyright 2018-present 王用军
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package cloud.wangyongjun.vxmq.assist;

import io.netty.handler.codec.mqtt.MqttProperties;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class MqttPropertiesUtil {

  private static final String ID_KEY = "id";
  private static final String KEY_KEY = "key";
  private static final String VALUE_KEY = "value";
  private static final String TYPE_KEY = "type";

  public static JsonArray encode(MqttProperties mqttProperties) {
    JsonArray jsonArray = new JsonArray();
    Collection<? extends MqttProperties.MqttProperty> allMqttProperties = mqttProperties.listAll();
    allMqttProperties.forEach(mqttProperty -> {
      if (mqttProperty instanceof MqttProperties.StringProperty) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.put(ID_KEY, mqttProperty.propertyId());
        jsonObject.put(VALUE_KEY, mqttProperty.value());
        jsonObject.put(TYPE_KEY, MqttProperties.StringProperty.class.getSimpleName());
        jsonArray.add(jsonObject);
      } else if (mqttProperty instanceof MqttProperties.IntegerProperty) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.put(ID_KEY, mqttProperty.propertyId());
        jsonObject.put(VALUE_KEY, mqttProperty.value());
        jsonObject.put(TYPE_KEY, MqttProperties.IntegerProperty.class.getSimpleName());
        jsonArray.add(jsonObject);
      } else if (mqttProperty instanceof MqttProperties.BinaryProperty) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.put(ID_KEY, mqttProperty.propertyId());
        jsonObject.put(VALUE_KEY, mqttProperty.value());
        jsonObject.put(TYPE_KEY, MqttProperties.BinaryProperty.class.getSimpleName());
        jsonArray.add(jsonObject);
      } else if (mqttProperty instanceof MqttProperties.UserProperties) {
        ((MqttProperties.UserProperties) mqttProperty).value().forEach(stringPair -> {
          JsonObject jsonObject = new JsonObject();
          jsonObject.put(ID_KEY, mqttProperty.propertyId());
          jsonObject.put(KEY_KEY, stringPair.key);
          jsonObject.put(VALUE_KEY, stringPair.value);
          jsonObject.put(TYPE_KEY, MqttProperties.UserProperty.class.getSimpleName());
          jsonArray.add(jsonObject);
        });
      }
    });
    return jsonArray;
  }

  public static JsonArray encodeUserProperties(List<MqttProperties.StringPair> userProperties) {
    return userProperties == null ? null :
      userProperties.stream()
        .map(stringPair -> new JsonObject().put("key", stringPair.key).put("value", stringPair.value))
        .collect(JsonArray::new, JsonArray::add, JsonArray::addAll);
  }

  public static List<MqttProperties.StringPair> decodeUserProperties(JsonArray jsonArray) {
    return jsonArray == null ? new ArrayList<>() :
      jsonArray.stream()
        .map(o -> (JsonObject) o)
        .map(j -> new MqttProperties.StringPair(j.getString("key"), j.getString("value")))
        .collect(Collectors.toList());
  }

  public static MqttProperties decode(JsonArray jsonArray) {
    MqttProperties mqttProperties = new MqttProperties();
    jsonArray.forEach(o -> {
      JsonObject jsonObject = (JsonObject) o;
      int id = jsonObject.getInteger(ID_KEY);
      String type = jsonObject.getString(TYPE_KEY);
      if (type.equals(MqttProperties.StringProperty.class.getSimpleName())) {
        MqttProperties.StringProperty stringProperty = new MqttProperties.StringProperty(id, jsonObject.getString("value"));
        mqttProperties.add(stringProperty);
      } else if (type.equals(MqttProperties.IntegerProperty.class.getSimpleName())) {
        MqttProperties.IntegerProperty integerProperty = new MqttProperties.IntegerProperty(id, jsonObject.getInteger("value"));
        mqttProperties.add(integerProperty);
      } else if (type.equals(MqttProperties.BinaryProperty.class.getSimpleName())) {
        MqttProperties.BinaryProperty binaryProperty = new MqttProperties.BinaryProperty(id, jsonObject.getBinary("value"));
        mqttProperties.add(binaryProperty);
      } else if (type.equals(MqttProperties.UserProperty.class.getSimpleName())) {
        MqttProperties.UserProperty userProperty = new MqttProperties.UserProperty(jsonObject.getString("key"), jsonObject.getString("value"));
        mqttProperties.add(userProperty);
      }
    });
    return mqttProperties;
  }

  /**
   * Get value from {@link MqttProperties}
   * @param mqttProperties {@link MqttProperties}
   * @param mqttPropertyType {@link io.netty.handler.codec.mqtt.MqttProperties.MqttPropertyType}
   * @param clazz could only be: {@link io.netty.handler.codec.mqtt.MqttProperties.IntegerProperty}, {@link io.netty.handler.codec.mqtt.MqttProperties.StringProperty}, {@link io.netty.handler.codec.mqtt.MqttProperties.BinaryProperty}
   * @return Mqtt property values
   */
  public static <T> T getValue(MqttProperties mqttProperties, MqttProperties.MqttPropertyType mqttPropertyType, Class<? extends MqttProperties.MqttProperty<T>> clazz) {
    return Optional.ofNullable(mqttProperties.getProperty(mqttPropertyType.value())).map(mqttProperty -> (T) mqttProperty.value()).orElse(null);
  }

  /**
   * Get values from {@link MqttProperties}
   * @param mqttProperties {@link MqttProperties}
   * @param mqttPropertyType could only be USER_PROPERTY, SUBSCRIPTION_IDENTIFIER
   * @param clazz could only be: {@link io.netty.handler.codec.mqtt.MqttProperties.UserProperty}, {@link io.netty.handler.codec.mqtt.MqttProperties.IntegerProperty}
   * @return Mqtt property values
   */
  public static <T> List<T> getValues(MqttProperties mqttProperties, MqttProperties.MqttPropertyType mqttPropertyType, Class<? extends MqttProperties.MqttProperty<T>> clazz) {
    if (!List.of(MqttProperties.MqttPropertyType.USER_PROPERTY, MqttProperties.MqttPropertyType.SUBSCRIPTION_IDENTIFIER).contains(mqttPropertyType)) {
      throw new IllegalArgumentException("Not support " + mqttPropertyType);
    }
    if (!(clazz.equals(MqttProperties.UserProperty.class) || clazz.equals(MqttProperties.IntegerProperty.class))) {
      throw new IllegalArgumentException("Not support " + clazz);
    }
    return mqttProperties.getProperties(mqttPropertyType.value()).stream().map(mp -> (T) mp.value()).toList();
  }

}
