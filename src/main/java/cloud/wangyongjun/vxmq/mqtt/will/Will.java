package cloud.wangyongjun.vxmq.mqtt.will;

import cloud.wangyongjun.vxmq.assist.Nullable;
import cloud.wangyongjun.vxmq.mqtt.StringPair;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Will {

  private String sessionId;
  private String clientId;
  private String willTopicName;  // don't contain wildcard
  private Buffer willMessage;
  private int willQos;
  private boolean willRetain;
  // MqttProperties
  private Integer willDelayInterval;  // 3.1.3.2.2 Will Delay Interval, in seconds
  private Integer payloadFormatIndicator;  // 3.1.3.2.3 Payload Format Indicator
  private Integer messageExpiryInterval;  // 3.1.3.2.4 Message Expiry Interval
  private String contentType;  // 3.1.3.2.5 Content Type
  private String responseTopic;  // 3.1.3.2.6 Response Topic
  private Buffer correlationData;  // 3.1.3.2.7 Correlation Data
  private List<StringPair> userProperties;  // 3.1.3.2.8 User Property

  private long createdTime;

  public Will() {
  }

  public Will(JsonObject jsonObject) {
    this.sessionId = jsonObject.getString("sessionId");
    this.clientId = jsonObject.getString("clientId");
    this.willTopicName = jsonObject.getString("willTopicName");
    this.willMessage = jsonObject.getBuffer("willMessage");
    this.willQos = jsonObject.getInteger("willQos");
    this.willRetain = jsonObject.getBoolean("willRetain");
    this.willDelayInterval = jsonObject.getInteger("willDelayInterval");
    this.payloadFormatIndicator = jsonObject.getInteger("payloadFormatIndicator");
    this.messageExpiryInterval = jsonObject.getInteger("messageExpiryInterval");
    this.contentType = jsonObject.getString("contentType");
    this.responseTopic = jsonObject.getString("responseTopic");
    this.correlationData = jsonObject.getBuffer("correlationData");
    this.userProperties = jsonObject.getJsonArray("userProperties") == null ? new ArrayList<>() : jsonObject.getJsonArray("userProperties").stream().map(o -> (JsonObject) o).map(StringPair::new).collect(Collectors.toList());
    this.createdTime = Instant.parse(jsonObject.getString("createdTime")).toEpochMilli();
  }

  public JsonObject toJson() {
    JsonObject jsonObject = new JsonObject();
    jsonObject.put("sessionId", this.sessionId);
    jsonObject.put("clientId", this.clientId);
    jsonObject.put("willTopicName", this.willTopicName);
    jsonObject.put("willMessage", this.willMessage);
    jsonObject.put("willQos", this.willQos);
    jsonObject.put("willRetain", this.willRetain);
    jsonObject.put("willDelayInterval", this.willDelayInterval);
    jsonObject.put("payloadFormatIndicator", this.payloadFormatIndicator);
    jsonObject.put("messageExpiryInterval", this.messageExpiryInterval);
    jsonObject.put("contentType", this.contentType);
    jsonObject.put("responseTopic", this.responseTopic);
    jsonObject.put("correlationData", this.correlationData);
    jsonObject.put("userProperties", this.userProperties == null ? null : this.userProperties.stream().map(StringPair::toJson).collect(JsonArray::new, JsonArray::add, JsonArray::addAll));
    jsonObject.put("createdTime", Instant.ofEpochMilli(this.createdTime).toString());
    return jsonObject;
  }

  @Override
  public String toString() {
    return toJson().toString();
  }

  public String getSessionId() {
    return sessionId;
  }

  public String getClientId() {
    return clientId;
  }

  public String getWillTopicName() {
    return willTopicName;
  }

  public Buffer getWillMessage() {
    return willMessage;
  }

  public int getWillQos() {
    return willQos;
  }

  public boolean isWillRetain() {
    return willRetain;
  }

  @Nullable
  public Integer getWillDelayInterval() {
    return willDelayInterval;
  }

  @Nullable
  public Integer getPayloadFormatIndicator() {
    return payloadFormatIndicator;
  }

  @Nullable
  public Integer getMessageExpiryInterval() {
    return messageExpiryInterval;
  }

  @Nullable
  public String getContentType() {
    return contentType;
  }

  @Nullable
  public String getResponseTopic() {
    return responseTopic;
  }

  @Nullable
  public Buffer getCorrelationData() {
    return correlationData;
  }

  @Nullable
  public List<StringPair> getUserProperties() {
    return userProperties;
  }

  public long getCreatedTime() {
    return createdTime;
  }

  public Will setSessionId(String sessionId) {
    this.sessionId = sessionId;
    return this;
  }

  public Will setClientId(String clientId) {
    this.clientId = clientId;
    return this;
  }

  public Will setWillTopicName(String willTopicName) {
    this.willTopicName = willTopicName;
    return this;
  }

  public Will setWillMessage(Buffer willMessage) {
    this.willMessage = willMessage;
    return this;
  }

  public Will setWillQos(int willQos) {
    this.willQos = willQos;
    return this;
  }

  public Will setWillRetain(boolean willRetain) {
    this.willRetain = willRetain;
    return this;
  }

  public Will setWillDelayInterval(Integer willDelayInterval) {
    this.willDelayInterval = willDelayInterval;
    return this;
  }

  public Will setPayloadFormatIndicator(Integer payloadFormatIndicator) {
    this.payloadFormatIndicator = payloadFormatIndicator;
    return this;
  }

  public Will setMessageExpiryInterval(Integer messageExpiryInterval) {
    this.messageExpiryInterval = messageExpiryInterval;
    return this;
  }

  public Will setContentType(String contentType) {
    this.contentType = contentType;
    return this;
  }

  public Will setResponseTopic(String responseTopic) {
    this.responseTopic = responseTopic;
    return this;
  }

  public Will setCorrelationData(Buffer correlationData) {
    this.correlationData = correlationData;
    return this;
  }

  public Will setUserProperties(List<StringPair> userProperties) {
    this.userProperties = userProperties;
    return this;
  }

  public Will addUserProperty(String key, String value) {
    if (this.userProperties == null) {
      userProperties = new ArrayList<>();
    }
    userProperties.add(new StringPair(key, value));
    return this;
  }

  public Will setCreatedTime(long createdTime) {
    this.createdTime = createdTime;
    return this;
  }
}
