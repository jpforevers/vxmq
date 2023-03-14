package cloud.wangyongjun.vxmq.mqtt.msg;

import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;

public class MsgToTopic {

  private String clientId;
  private String topic;
  private int qos;
  private Buffer payload;
  private boolean retain;

  public MsgToTopic() {
  }

  public MsgToTopic(JsonObject jsonObject) {
    this.clientId = jsonObject.getString("clientId");
    this.topic = jsonObject.getString("topic");
    this.qos = jsonObject.getInteger("qos");
    this.payload = jsonObject.getBuffer("payload");
    this.retain = jsonObject.getBoolean("retain");
  }

  public JsonObject toJson() {
    JsonObject jsonObject = new JsonObject();
    jsonObject.put("clientId", this.clientId);
    jsonObject.put("topic", this.topic);
    jsonObject.put("qos", this.qos);
    jsonObject.put("payload", this.payload);
    jsonObject.put("retain", this.retain);
    return jsonObject;
  }

  @Override
  public String toString() {
    return toJson().toString();
  }

  public String getClientId() {
    return clientId;
  }

  public MsgToTopic setClientId(String clientId) {
    this.clientId = clientId;
    return this;
  }

  public String getTopic() {
    return topic;
  }

  public MsgToTopic setTopic(String topic) {
    this.topic = topic;
    return this;
  }

  public int getQos() {
    return qos;
  }

  public MsgToTopic setQos(int qos) {
    this.qos = qos;
    return this;
  }

  public Buffer getPayload() {
    return payload;
  }

  public MsgToTopic setPayload(Buffer payload) {
    this.payload = payload;
    return this;
  }

  public boolean isRetain() {
    return retain;
  }

  public MsgToTopic setRetain(boolean retain) {
    this.retain = retain;
    return this;
  }

}
