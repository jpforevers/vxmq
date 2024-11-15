/*
 * Copyright (C) 2023-2024 王用军
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package io.github.jpforevers.vxmq.service.session;

import io.github.jpforevers.vxmq.assist.ModelConstants;
import io.github.jpforevers.vxmq.assist.Nullable;
import io.vertx.core.json.JsonObject;

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
    this.createdTime = jsonObject.getLong(ModelConstants.FIELD_NAME_CREATED_TIME);
    this.updatedTime = jsonObject.getLong(ModelConstants.FIELD_NAME_UPDATED_TIME);
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
    jsonObject.put(ModelConstants.FIELD_NAME_CREATED_TIME, this.createdTime);
    jsonObject.put(ModelConstants.FIELD_NAME_UPDATED_TIME, this.updatedTime);
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

  public enum Field {
    sessionId, clientId, online, verticleId, nodeId, cleanSession,
    keepAlive, protocolLevel, sessionExpiryInterval, createdTime, updatedTime
  }

}
