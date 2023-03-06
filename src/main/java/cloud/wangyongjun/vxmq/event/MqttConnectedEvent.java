package cloud.wangyongjun.vxmq.event;

import io.vertx.core.json.JsonObject;

public class MqttConnectedEvent implements MqttEvent {

  private long time;
  private EventType eventType;
  private String nodeId;
  private String clientId;
  private int protocolVersion;

  public MqttConnectedEvent() {
  }

  public MqttConnectedEvent(long time, EventType eventType, String nodeId, String clientId, int protocolVersion) {
    this.time = time;
    this.eventType = eventType;
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
    return eventType;
  }

  @Override
  public JsonObject toJson() {
    JsonObject jsonObject = new JsonObject();
    jsonObject.put("time", time);
    jsonObject.put("eventType", eventType.name());
    jsonObject.put("clientId", clientId);
    jsonObject.put("protocolVersion", protocolVersion);
    return jsonObject;
  }

  @Override
  public MqttConnectedEvent fromJson(JsonObject jsonObject) {
    this.time = jsonObject.getLong("time");
    this.eventType = EventType.valueOf(jsonObject.getString("eventType"));
    this.clientId = jsonObject.getString("clientId");
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
