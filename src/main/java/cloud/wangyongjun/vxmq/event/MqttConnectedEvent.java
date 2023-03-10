package cloud.wangyongjun.vxmq.event;

import io.vertx.core.json.JsonObject;

public class MqttConnectedEvent implements MqttEvent {

  private long time;
  private String nodeId;
  private String clientId;
  private int protocolVersion;

  public MqttConnectedEvent() {
  }

  public MqttConnectedEvent(long time, String nodeId, String clientId, int protocolVersion) {
    this.time = time;
    this.nodeId = nodeId;
    this.clientId = clientId;
    this.protocolVersion = protocolVersion;
  }

  @Override
  public long getTime() {
    return time;
  }

  @Override
  public EventType getEventType() {
    return EventType.MQTT_CONNECTED_EVENT;
  }

  @Override
  public JsonObject toJson() {
    JsonObject jsonObject = new JsonObject();
    jsonObject.put("time", time);
    jsonObject.put("eventType", getEventType().name());
    jsonObject.put("nodeId", nodeId);
    jsonObject.put("local", isLocal());
    jsonObject.put("clientId", clientId);
    jsonObject.put("protocolVersion", protocolVersion);
    return jsonObject;
  }

  @Override
  public MqttConnectedEvent fromJson(JsonObject jsonObject) {
    this.time = jsonObject.getLong("time");
    this.nodeId = jsonObject.getString("nodeId");
    this.clientId = jsonObject.getString("clientId");
    this.protocolVersion = jsonObject.getInteger("protocolVersion");
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

  public int getProtocolVersion() {
    return protocolVersion;
  }

}
