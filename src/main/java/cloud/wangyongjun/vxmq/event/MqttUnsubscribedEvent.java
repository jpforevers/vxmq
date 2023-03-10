package cloud.wangyongjun.vxmq.event;

import io.vertx.core.json.JsonObject;

public class MqttUnsubscribedEvent implements MqttEvent{

  private long time;
  private EventType eventType;
  private String nodeId;
  private boolean local;
  private String clientId;
  private String sessionId;
  private String topic;

  public MqttUnsubscribedEvent() {
  }

  public MqttUnsubscribedEvent(long time, EventType eventType, String nodeId, boolean local, String clientId, String sessionId, String topic) {
    this.time = time;
    this.eventType = eventType;
    this.nodeId = nodeId;
    this.local = local;
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
    return eventType;
  }

  @Override
  public JsonObject toJson() {
    JsonObject jsonObject = new JsonObject();
    jsonObject.put("time", time);
    jsonObject.put("eventType", eventType.name());
    jsonObject.put("nodeId", nodeId);
    jsonObject.put("local", local);
    jsonObject.put("clientId", clientId);
    jsonObject.put("sessionId", sessionId);
    jsonObject.put("topic", topic);
    return jsonObject;
  }

  @Override
  public Event fromJson(JsonObject jsonObject) {
    this.time = jsonObject.getLong("time");
    this.eventType = EventType.valueOf(jsonObject.getString("eventType"));
    this.nodeId = jsonObject.getString("nodeId");
    this.local = jsonObject.getBoolean("local");
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
  public boolean isLocal() {
    return local;
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
