package cloud.wangyongjun.vxmq.mqtt;

import io.netty.handler.codec.mqtt.MqttProperties;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.Collection;

public class MqttPropertiesUtil {

  private static final String ID_KEY = "id";
  private static final String KEY_KEY = "key";
  private static final String VALUE_KEY = "value";
  private static final String TYPE_KEY = "type";

  public static JsonArray encode(MqttProperties mqttProperties) {
    JsonArray jsonArray = new JsonArray();
    Collection<? extends MqttProperties.MqttProperty> alllMqttProperties = mqttProperties.listAll();
    alllMqttProperties.forEach(mqttProperty -> {
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

}
