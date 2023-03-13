package cloud.wangyongjun.vxmq.event;

import io.vertx.core.json.JsonObject;
import io.vertx.mqtt.messages.codes.MqttDisconnectReasonCode;

public class MqttDisconnectedEvent implements MqttEvent {

  private long time;
  private String nodeId;
  private String clientId;
  private String sessionId;
  private MqttDisconnectReasonCode code;

  public MqttDisconnectedEvent() {
  }

  public MqttDisconnectedEvent(long time, String nodeId, String clientId, String sessionId, MqttDisconnectReasonCode code) {
    this.time = time;
    this.nodeId = nodeId;
    this.clientId = clientId;
    this.sessionId = sessionId;
    this.code = code;
  }

  @Override
  public long getTime() {
    return time;
  }

  @Override
  public EventType getEventType() {
    return EventType.MQTT_DISCONNECTED_EVENT;
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
    jsonObject.put("code", code);
    return jsonObject;
  }

  @Override
  public MqttDisconnectedEvent fromJson(JsonObject jsonObject) {
    this.time = jsonObject.getLong("time");
    this.nodeId = jsonObject.getString("nodeId");
    this.clientId = jsonObject.getString("clientId");
    return this;
  }

  @Override
  public String getClientId() {
    return clientId;
  }

  public String getSessionId() {
    return sessionId;
  }

  public MqttDisconnectReasonCode getCode() {
    return code;
  }

  @Override
  public String getNodeId() {
    return nodeId;
  }

}
