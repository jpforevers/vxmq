package cloud.wangyongjun.vxmq.event;

import io.vertx.core.json.JsonObject;

public class MqttEndpointClosedEvent implements MqttEvent{

  private long time;
  private EventType eventType;
  private String clientId;
  private String sessionId;

  public MqttEndpointClosedEvent() {
  }

  public MqttEndpointClosedEvent(long time, EventType eventType, String clientId, String sessionId) {
    this.time = time;
    this.eventType = eventType;
    this.clientId = clientId;
    this.sessionId = sessionId;
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
    return jsonObject;
  }

  @Override
  public MqttEndpointClosedEvent fromJson(JsonObject jsonObject) {
    this.time = jsonObject.getLong("time");
    this.eventType = EventType.valueOf(jsonObject.getString("eventType"));
    this.clientId = jsonObject.getString("clientId");
    this.sessionId = jsonObject.getString("sessionId");
    return this;
  }

  @Override
  public String getClientId() {
    return clientId;
  }

  public String getSessionId() {
    return sessionId;
  }
}
