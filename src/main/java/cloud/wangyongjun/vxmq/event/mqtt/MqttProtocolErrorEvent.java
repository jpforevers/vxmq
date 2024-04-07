package cloud.wangyongjun.vxmq.event.mqtt;

import cloud.wangyongjun.vxmq.event.EventType;
import io.vertx.core.json.JsonObject;

public class MqttProtocolErrorEvent implements MqttEvent{

  private long time;
  private String nodeId;
  private String clientId;
  private String reason;

  public MqttProtocolErrorEvent() {
  }

  public MqttProtocolErrorEvent(long time, String nodeId, String clientId, String reason) {
    this.time = time;
    this.nodeId = nodeId;
    this.clientId = clientId;
    this.reason = reason;
  }

  @Override
  public JsonObject toJson() {
    JsonObject jsonObject = new JsonObject();
    jsonObject.put("time", time);
    jsonObject.put("eventType", getEventType().name());
    jsonObject.put("nodeId", nodeId);
    jsonObject.put("local", isLocal());
    jsonObject.put("clientId", clientId);
    jsonObject.put("reason", reason);
    return jsonObject;
  }

  @Override
  public MqttProtocolErrorEvent fromJson(JsonObject jsonObject) {
    this.time = jsonObject.getLong("time");
    this.nodeId = jsonObject.getString("nodeId");
    this.clientId = jsonObject.getString("clientId");
    this.reason = jsonObject.getString("reason");
    return this;
  }

  @Override
  public long getTime() {
    return time;
  }

  @Override
  public EventType getEventType() {
    return EventType.EVENT_MQTT_PROTOCOL_ERROR;
  }

  @Override
  public String getNodeId() {
    return nodeId;
  }

  @Override
  public String getClientId() {
    return clientId;
  }

  public String getReason() {
    return reason;
  }

}
