package cloud.wangyongjun.vxmq.mqtt.session;

import cloud.wangyongjun.vxmq.assist.Nullable;
import io.vertx.core.json.JsonObject;
import org.apache.ignite.cache.affinity.AffinityKeyMapped;

import java.time.Instant;

public class Session {

  private String sessionId;
  private String clientId;
  private boolean online;
  private String verticleId;
  @AffinityKeyMapped
  private String nodeId;
  private boolean cleanSession;
  private int protocolLevel;
  // MqttProperties
  private Integer sessionExpiryInterval;  // seconds

  private long createdTime;
  private long updatedTime;

  public Session() {
  }

  public Session(JsonObject jsonObject) {
    this.sessionId = jsonObject.getString("sessionId");
    this.clientId = jsonObject.getString("clientId");
    this.online = jsonObject.getBoolean("online");
    this.verticleId = jsonObject.getString("verticleId");
    this.nodeId = jsonObject.getString("nodeId");
    this.cleanSession = jsonObject.getBoolean("cleanSession");
    this.protocolLevel = jsonObject.getInteger("protocolLevel");
    this.sessionExpiryInterval = jsonObject.getInteger("sessionExpiryInterval");
    this.createdTime = Instant.parse(jsonObject.getString("createdTime")).toEpochMilli();
    this.updatedTime = Instant.parse(jsonObject.getString("updatedTime")).toEpochMilli();
  }

  public JsonObject toJson() {
    JsonObject jsonObject = new JsonObject();
    jsonObject.put("sessionId", this.sessionId);
    jsonObject.put("clientId", this.clientId);
    jsonObject.put("online", this.online);
    jsonObject.put("verticleId", this.verticleId);
    jsonObject.put("nodeId", this.nodeId);
    jsonObject.put("cleanSession", this.cleanSession);
    jsonObject.put("protocolLevel", this.protocolLevel);
    jsonObject.put("sessionExpiryInterval", this.sessionExpiryInterval);
    jsonObject.put("createdTime", Instant.ofEpochMilli(this.createdTime).toString());
    jsonObject.put("updatedTime", Instant.ofEpochMilli(this.updatedTime).toString());
    return jsonObject;
  }

  public Session copy() {
    return new Session().setSessionId(sessionId).setClientId(clientId).setOnline(online).setVerticleId(verticleId)
      .setNodeId(nodeId).setCleanSession(cleanSession).setProtocolLevel(protocolLevel).setSessionExpiryInterval(sessionExpiryInterval)
      .setCreatedTime(createdTime).setUpdatedTime(updatedTime);
  }

  @Override
  public String toString() {
    return toJson().toString();
  }

  public String getSessionId() {
    return sessionId;
  }

  public Session setSessionId(String sessionId) {
    this.sessionId = sessionId;
    return this;
  }

  public String getClientId() {
    return clientId;
  }

  public Session setClientId(String clientId) {
    this.clientId = clientId;
    return this;
  }

  public boolean isOnline() {
    return online;
  }

  public Session setOnline(boolean online) {
    this.online = online;
    return this;
  }

  public String getVerticleId() {
    return verticleId;
  }

  public Session setVerticleId(String verticleId) {
    this.verticleId = verticleId;
    return this;
  }

  public String getNodeId() {
    return nodeId;
  }

  public Session setNodeId(String nodeId) {
    this.nodeId = nodeId;
    return this;
  }

  public boolean isCleanSession() {
    return cleanSession;
  }

  public Session setCleanSession(boolean cleanSession) {
    this.cleanSession = cleanSession;
    return this;
  }

  public int getProtocolLevel() {
    return protocolLevel;
  }

  public Session setProtocolLevel(int protocolLevel) {
    this.protocolLevel = protocolLevel;
    return this;
  }

  @Nullable
  public Integer getSessionExpiryInterval() {
    return sessionExpiryInterval;
  }

  public Session setSessionExpiryInterval(Integer sessionExpiryInterval) {
    this.sessionExpiryInterval = sessionExpiryInterval;
    return this;
  }

  public long getCreatedTime() {
    return createdTime;
  }

  public Session setCreatedTime(long createdTime) {
    this.createdTime = createdTime;
    return this;
  }

  public long getUpdatedTime() {
    return updatedTime;
  }

  public Session setUpdatedTime(long updatedTime) {
    this.updatedTime = updatedTime;
    return this;
  }
}
