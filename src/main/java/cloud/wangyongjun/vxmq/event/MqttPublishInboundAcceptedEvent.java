package cloud.wangyongjun.vxmq.event;

import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;

public class MqttPublishInboundAcceptedEvent implements MqttEvent{

  private long time;
  private String nodeId;
  private String clientId;
  private String sessionId;
  private String topic;
  private int qos;
  private int messageId;
  private Buffer payload;
  private boolean dup;
  private boolean retain;

  public MqttPublishInboundAcceptedEvent() {
  }

  public MqttPublishInboundAcceptedEvent(long time, String nodeId, String clientId, String sessionId, String topic, int qos, int messageId, Buffer payload, boolean dup, boolean retain) {
    this.time = time;
    this.nodeId = nodeId;
    this.clientId = clientId;
    this.sessionId = sessionId;
    this.topic = topic;
    this.qos = qos;
    this.messageId = messageId;
    this.payload = payload;
    this.dup = dup;
    this.retain = retain;
  }

  @Override
  public long getTime() {
    return time;
  }

  @Override
  public EventType getEventType() {
    return EventType.MQTT_PUBLISH_INBOUND_ACCEPTED_EVENT;
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
    jsonObject.put("sessionId", sessionId);
    jsonObject.put("topic", topic);
    jsonObject.put("qos", qos);
    jsonObject.put("messageId", messageId);
    jsonObject.put("payload", payload);
    jsonObject.put("dup", dup);
    jsonObject.put("retain", retain);
    return jsonObject;
  }

  @Override
  public MqttPublishInboundAcceptedEvent fromJson(JsonObject jsonObject) {
    this.time = jsonObject.getLong("time");
    this.nodeId = jsonObject.getString("nodeId");
    this.clientId = jsonObject.getString("clientId");
    this.sessionId = jsonObject.getString("sessionId");
    this.topic = jsonObject.getString("topic");
    this.qos = jsonObject.getInteger("qos");
    this.messageId = jsonObject.getInteger("messageId");
    this.payload = jsonObject.getBuffer("payload");
    this.dup = jsonObject.getBoolean("dup");
    this.retain = jsonObject.getBoolean("retain");
    return this;
  }

  @Override
  public String getClientId() {
    return clientId;
  }

  public String getSessionId() {
    return sessionId;
  }

  public String getTopic() {
    return topic;
  }

  public int getQos() {
    return qos;
  }

  public int getMessageId() {
    return messageId;
  }

  public Buffer getPayload() {
    return payload;
  }

  public boolean isDup() {
    return dup;
  }

  public boolean isRetain() {
    return retain;
  }
}
