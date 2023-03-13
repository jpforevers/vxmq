package cloud.wangyongjun.vxmq.event;

import io.vertx.core.json.JsonObject;

public class MqttUnsubscribedEvent implements MqttEvent{

  private long time;
  private String nodeId;
  private String clientId;
  private String sessionId;
  private String topic;

  public MqttUnsubscribedEvent() {
  }

  public MqttUnsubscribedEvent(long time, String nodeId, String clientId, String sessionId, String topic) {
    this.time = time;
    this.nodeId = nodeId;
    this.clientId = clientId;
    this.sessionId = sessionId;
    this.topic = topic;
  }

  @Override
  public long getTime() {
    return time;
  }

  @Override
  public EventType getEventType() {
    return EventType.MQTT_UNSUBSCRIBED_EVENT;
  }

  @Override
  public JsonObject toJson() {
    JsonObject jsonObject = new JsonObject();
    jsonObject.put("time", time);
    jsonObject.put("eventType", getEventType().name());
    jsonObject.put("nodeId", nodeId);
    jsonObject.put("local", isLocal());
    jsonObject.put("clientId", clientId);
    jsonObject.put("sessionId", sessionId);
    jsonObject.put("topic", topic);
    return jsonObject;
  }

  @Override
  public Event fromJson(JsonObject jsonObject) {
    this.time = jsonObject.getLong("time");
    this.nodeId = jsonObject.getString("nodeId");
    this.clientId = jsonObject.getString("clientId");
    this.sessionId = jsonObject.getString("sessionId");
    this.topic = jsonObject.getString("topic");
    return this;
  }

  @Override
  public String getNodeId() {
    return nodeId;
  }

  @Override
  public String getClientId() {
    return clientId;
  }

  public String getTopic() {
    return topic;
  }

  public String getSessionId() {
    return sessionId;
  }
}
