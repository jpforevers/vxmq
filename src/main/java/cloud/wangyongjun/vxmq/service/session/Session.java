/*
 * Copyright 2018-present 王用军
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package cloud.wangyongjun.vxmq.service.session;

import cloud.wangyongjun.vxmq.assist.ModelConstants;
import cloud.wangyongjun.vxmq.assist.Nullable;
import io.vertx.core.json.JsonObject;

import java.time.Instant;

public class Session {

  private String sessionId;
  private String clientId;
  private boolean online;
  private String verticleId;
  private String nodeId;
  private boolean cleanSession;
  private int keepAlive;
  private int protocolLevel;
  // MqttProperties
  private Integer sessionExpiryInterval;  // seconds

  private long createdTime;
  private long updatedTime;

  public Session() {
  }

  public Session(JsonObject jsonObject) {
    this.sessionId = jsonObject.getString(ModelConstants.FIELD_NAME_SESSION_ID);
    this.clientId = jsonObject.getString(ModelConstants.FIELD_NAME_CLIENT_ID);
    this.online = jsonObject.getBoolean(ModelConstants.FIELD_NAME_ONLINE);
    this.verticleId = jsonObject.getString(ModelConstants.FIELD_NAME_VERTICLE_ID);
    this.nodeId = jsonObject.getString(ModelConstants.FIELD_NAME_NODE_ID);
    this.cleanSession = jsonObject.getBoolean(ModelConstants.FIELD_NAME_CLEAN_SESSION);
    this.keepAlive = jsonObject.getInteger(ModelConstants.FIELD_NAME_KEEP_ALIVE);
    this.protocolLevel = jsonObject.getInteger(ModelConstants.FIELD_NAME_PROTOCOL_LEVEL);
    this.sessionExpiryInterval = jsonObject.getInteger(ModelConstants.FIELD_NAME_SESSION_EXPIRY_INTERVAL);
    this.createdTime = Instant.parse(jsonObject.getString(ModelConstants.FIELD_NAME_CREATED_TIME)).toEpochMilli();
    this.updatedTime = Instant.parse(jsonObject.getString(ModelConstants.FIELD_NAME_UPDATED_TIME)).toEpochMilli();
  }

  public JsonObject toJson() {
    JsonObject jsonObject = new JsonObject();
    jsonObject.put(ModelConstants.FIELD_NAME_SESSION_ID, this.sessionId);
    jsonObject.put(ModelConstants.FIELD_NAME_CLIENT_ID, this.clientId);
    jsonObject.put(ModelConstants.FIELD_NAME_ONLINE, this.online);
    jsonObject.put(ModelConstants.FIELD_NAME_VERTICLE_ID, this.verticleId);
    jsonObject.put(ModelConstants.FIELD_NAME_NODE_ID, this.nodeId);
    jsonObject.put(ModelConstants.FIELD_NAME_CLEAN_SESSION, this.cleanSession);
    jsonObject.put(ModelConstants.FIELD_NAME_KEEP_ALIVE, this.keepAlive);
    jsonObject.put(ModelConstants.FIELD_NAME_PROTOCOL_LEVEL, this.protocolLevel);
    jsonObject.put(ModelConstants.FIELD_NAME_SESSION_EXPIRY_INTERVAL, this.sessionExpiryInterval);
    jsonObject.put(ModelConstants.FIELD_NAME_CREATED_TIME, Instant.ofEpochMilli(this.createdTime).toString());
    jsonObject.put(ModelConstants.FIELD_NAME_UPDATED_TIME, Instant.ofEpochMilli(this.updatedTime).toString());
    return jsonObject;
  }

  public Session copy() {
    return new Session().setSessionId(sessionId).setClientId(clientId).setOnline(online).setVerticleId(verticleId)
      .setNodeId(nodeId).setCleanSession(cleanSession).setKeepAlive(keepAlive)
      .setProtocolLevel(protocolLevel).setSessionExpiryInterval(sessionExpiryInterval)
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

  public int getKeepAlive() {
    return keepAlive;
  }

  public Session setKeepAlive(int keepAlive) {
    this.keepAlive = keepAlive;
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
