package cloud.wangyongjun.vxmq.event;

import io.vertx.core.json.JsonObject;

public class MqttSessionTakenOverEvent implements MqttEvent{

  private long time;
  private String nodeId;
  private String clientId;
  private String oldSessionId;
  private String newSessionId;

  public MqttSessionTakenOverEvent() {
  }

  public MqttSessionTakenOverEvent(long time, String nodeId, String clientId, String oldSessionId, String newSessionId) {
    this.time = time;
    this.nodeId = nodeId;
    this.clientId = clientId;
    this.oldSessionId = oldSessionId;
    this.newSessionId = newSessionId;
  }

  @Override
  public JsonObject toJson() {
    JsonObject jsonObject = new JsonObject();
    jsonObject.put("time", time);
    jsonObject.put("eventType", getEventType().name());
    jsonObject.put("nodeId", nodeId);
    jsonObject.put("local", isLocal());
    jsonObject.put("clientId", clientId);
    jsonObject.put("oldSessionId", oldSessionId);
    jsonObject.put("newSessionId", newSessionId);
    return jsonObject;
  }

  @Override
  public MqttSessionTakenOverEvent fromJson(JsonObject jsonObject) {
    this.time = jsonObject.getLong("time");
    this.nodeId = jsonObject.getString("nodeId");
    this.clientId = jsonObject.getString("clientId");
    this.oldSessionId = jsonObject.getString("oldSessionId");
    this.newSessionId = jsonObject.getString("newSessionId");
    return this;
  }

  @Override
  public long getTime() {
    return time;
  }

  @Override
  public EventType getEventType() {
    return EventType.EVENT_MQTT_SESSION_TAKEN_OVER;
  }

  @Override
  public String getNodeId() {
    return nodeId;
  }

  @Override
  public String getClientId() {
    return clientId;
  }

  public String getOldSessionId() {
    return oldSessionId;
  }

  public String getNewSessionId() {
    return newSessionId;
  }

}
