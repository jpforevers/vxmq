package cloud.wangyongjun.vxmq.mqtt;

import io.vertx.core.json.JsonObject;

public class StringPair {

  private String key;
  private String value;

  public StringPair() {
  }

  public StringPair(String key, String value) {
    this.key = key;
    this.value = value;
  }

  public StringPair(JsonObject jsonObject) {
    this.key = jsonObject.getString("key");
    this.value = jsonObject.getString("value");
  }

  public JsonObject toJson() {
    JsonObject jsonObject = new JsonObject();
    jsonObject.put("key", key);
    jsonObject.put("value", value);
    return jsonObject;
  }

  public String getKey() {
    return key;
  }

  public String getValue() {
    return value;
  }
}
