package cloud.wangyongjun.vxmq.event;

import io.vertx.core.json.JsonObject;
import io.vertx.mqtt.messages.codes.MqttDisconnectReasonCode;

public class MqttDisconnectedEvent implements MqttEvent {

  private long time;
  private EventType eventType;
  private String clientId;
  private String sessionId;
  private MqttDisconnectReasonCode code;

  public MqttDisconnectedEvent() {
  }

  public MqttDisconnectedEvent(long time, EventType eventType, String clientId, String sessionId, MqttDisconnectReasonCode code) {
    this.time = time;
    this.eventType = eventType;
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
    return eventType;
  }

  @Override
  public JsonObject toJson() {
    JsonObject jsonObject = new JsonObject();
    jsonObject.put("time", time);
    jsonObject.put("eventType", eventType.name());
    jsonObject.put("clientId", clientId);
    jsonObject.put("sessionId", sessionId);
    jsonObject.put("code", code);
    return jsonObject;
  }

  @Override
  public MqttDisconnectedEvent fromJson(JsonObject jsonObject) {
    this.time = jsonObject.getLong("time");
    this.eventType = EventType.valueOf(jsonObject.getString("eventType"));
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
}
