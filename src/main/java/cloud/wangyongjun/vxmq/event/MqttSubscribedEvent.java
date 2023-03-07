package cloud.wangyongjun.vxmq.event;

import io.vertx.core.json.JsonObject;

public class MqttSubscribedEvent implements MqttEvent{

  private long time;
  private EventType eventType;
  private String nodeId;
  private boolean local;
  private String clientId;
  private String topic;
  private int qos;

  public MqttSubscribedEvent() {
  }

  public MqttSubscribedEvent(long time, EventType eventType, String nodeId, boolean local, String clientId, String topic, int qos) {
    this.time = time;
    this.eventType = eventType;
    this.nodeId = nodeId;
    this.local = local;
    this.clientId = clientId;
    this.topic = topic;
    this.qos = qos;
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
    jsonObject.put("topic", topic);
    jsonObject.put("qos", qos);
    return jsonObject;
  }

  @Override
  public Event fromJson(JsonObject jsonObject) {
    this.time = jsonObject.getLong("time");
    this.eventType = EventType.valueOf(jsonObject.getString("eventType"));
    this.nodeId = jsonObject.getString("nodeId");
    this.local = jsonObject.getBoolean("local");
    this.clientId = jsonObject.getString("clientId");
    this.topic = jsonObject.getString("topic");
    this.qos = jsonObject.getInteger("qos");
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

  public int getQos() {
    return qos;
  }
}
