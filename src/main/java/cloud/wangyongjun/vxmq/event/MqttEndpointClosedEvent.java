package cloud.wangyongjun.vxmq.event;

import io.vertx.core.json.JsonObject;

public class MqttEndpointClosedEvent implements MqttEvent{

  private long time;
  private String nodeId;
  private String clientId;
  private String sessionId;

  public MqttEndpointClosedEvent() {
  }

  public MqttEndpointClosedEvent(long time, String nodeId, String clientId, String sessionId) {
    this.time = time;
    this.nodeId = nodeId;
    this.clientId = clientId;
    this.sessionId = sessionId;
  }

  @Override
  public long getTime() {
    return time;
  }

  @Override
  public EventType getEventType() {
    return EventType.MQTT_ENDPOINT_CLOSED_EVENT;
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
    return jsonObject;
  }

  @Override
  public MqttEndpointClosedEvent fromJson(JsonObject jsonObject) {
    this.time = jsonObject.getLong("time");
    this.clientId = jsonObject.getString("clientId");
    this.nodeId = jsonObject.getString("nodeId");
    this.sessionId = jsonObject.getString("sessionId");
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

  public String getSessionId() {
    return sessionId;
  }
}
