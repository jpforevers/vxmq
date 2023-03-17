package cloud.wangyongjun.vxmq.event;

import io.vertx.core.json.JsonObject;

public class MqttPingEvent implements MqttEvent {

  private long time;
  private String nodeId;
  private String clientId;

  public MqttPingEvent() {
  }

  public MqttPingEvent(long time, String nodeId, String clientId) {
    this.time = time;
    this.nodeId = nodeId;
    this.clientId = clientId;
  }

  @Override
  public long getTime() {
    return time;
  }

  @Override
  public EventType getEventType() {
    return EventType.MQTT_PING_EVENT;
  }

  @Override
  public String getNodeId() {
    return nodeId;
  }

  @Override
  public JsonObject toJson() {
    JsonObject jsonObject = new JsonObject();
    jsonObject.put("time", time);
    jsonObject.put("eventType", getEventType().name());
    jsonObject.put("nodeId", nodeId);
    jsonObject.put("local", isLocal());
    jsonObject.put("clientId", clientId);
    return jsonObject;
  }

  @Override
  public MqttPingEvent fromJson(JsonObject jsonObject) {
    this.time = jsonObject.getLong("time");
    this.nodeId = jsonObject.getString("nodeId");
    this.clientId = jsonObject.getString("clientId");
    return this;
  }

  @Override
  public String getClientId() {
    return clientId;
  }

}
