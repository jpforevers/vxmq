package cloud.wangyongjun.vxmq.mqtt.msg;

import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;

import java.time.Instant;

public class MsgToClient {

  private String sessionId;
  private String clientId;
  private Integer messageId;
  private String topic;
  private int qos;
  private Buffer payload;
  private boolean dup;
  private boolean retain;

  private long createdTime;

  public MsgToClient() {
  }

  public MsgToClient(JsonObject jsonObject) {
    this.sessionId = jsonObject.getString("sessionId");
    this.clientId = jsonObject.getString("clientId");
    this.messageId = jsonObject.getInteger("messageId");
    this.topic = jsonObject.getString("topic");
    this.qos = jsonObject.getInteger("qos");
    this.payload = jsonObject.getBuffer("payload");
    this.dup = jsonObject.getBoolean("dup");
    this.retain = jsonObject.getBoolean("retain");
    this.createdTime = Instant.parse(jsonObject.getString("createdTime")).toEpochMilli();
  }

  public JsonObject toJson() {
    JsonObject jsonObject = new JsonObject();
    jsonObject.put("sessionId", this.sessionId);
    jsonObject.put("clientId", this.clientId);
    jsonObject.put("messageId", this.messageId);
    jsonObject.put("topic", this.topic);
    jsonObject.put("qos", this.qos);
    jsonObject.put("payload", this.payload);
    jsonObject.put("dup", this.dup);
    jsonObject.put("retain", this.retain);
    jsonObject.put("createdTime", Instant.ofEpochMilli(this.createdTime).toString());
    return jsonObject;
  }

  @Override
  public String toString() {
    return toJson().toString();
  }

  public String getSessionId() {
    return sessionId;
  }

  public MsgToClient setSessionId(String sessionId) {
    this.sessionId = sessionId;
    return this;
  }

  public String getClientId() {
    return clientId;
  }

  public MsgToClient setClientId(String clientId) {
    this.clientId = clientId;
    return this;
  }

  public Integer getMessageId() {
    return messageId;
  }

  public MsgToClient setMessageId(Integer messageId) {
    this.messageId = messageId;
    return this;
  }

  public String getTopic() {
    return topic;
  }

  public MsgToClient setTopic(String topic) {
    this.topic = topic;
    return this;
  }

  public int getQos() {
    return qos;
  }

  public MsgToClient setQos(int qos) {
    this.qos = qos;
    return this;
  }

  public Buffer getPayload() {
    return payload;
  }

  public MsgToClient setPayload(Buffer payload) {
    this.payload = payload;
    return this;
  }

  public boolean isDup() {
    return dup;
  }

  public MsgToClient setDup(boolean dup) {
    this.dup = dup;
    return this;
  }

  public boolean isRetain() {
    return retain;
  }

  public MsgToClient setRetain(boolean retain) {
    this.retain = retain;
    return this;
  }

  public long getCreatedTime() {
    return createdTime;
  }

  public MsgToClient setCreatedTime(long createdTime) {
    this.createdTime = createdTime;
    return this;
  }
}
