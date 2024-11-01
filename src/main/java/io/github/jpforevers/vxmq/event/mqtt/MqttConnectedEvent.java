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

package io.github.jpforevers.vxmq.event.mqtt;

import io.github.jpforevers.vxmq.event.EventType;
import io.vertx.core.json.JsonObject;

public class MqttConnectedEvent implements MqttEvent {

  private long time;
  private String nodeId;
  private String clientId;
  private int protocolVersion;
  private String username;
  private String password;

  public MqttConnectedEvent() {
  }

  public MqttConnectedEvent(long time, String nodeId, String clientId, int protocolVersion, String username, String password) {
    this.time = time;
    this.nodeId = nodeId;
    this.clientId = clientId;
    this.protocolVersion = protocolVersion;
    this.username = username;
    this.password = password;
  }

  @Override
  public long getTime() {
    return time;
  }

  @Override
  public EventType getEventType() {
    return EventType.EVENT_MQTT_CONNECTED;
  }

  @Override
  public JsonObject toJson() {
    JsonObject jsonObject = new JsonObject();
    jsonObject.put("time", time);
    jsonObject.put("eventType", getEventType().name());
    jsonObject.put("nodeId", nodeId);
    jsonObject.put("local", isLocal());
    jsonObject.put("clientId", clientId);
    jsonObject.put("protocolVersion", protocolVersion);
    jsonObject.put("username", username);
    jsonObject.put("password", password);
    return jsonObject;
  }

  @Override
  public MqttConnectedEvent fromJson(JsonObject jsonObject) {
    this.time = jsonObject.getLong("time");
    this.nodeId = jsonObject.getString("nodeId");
    this.clientId = jsonObject.getString("clientId");
    this.protocolVersion = jsonObject.getInteger("protocolVersion");
    this.username = jsonObject.getString("username");
    this.password = jsonObject.getString("password");
    return this;
  }

  @Override
  public String getNodeId() {
    return nodeId;
  }

  @Override
  public String getClientId() {
    return clientId;
  }

  public int getProtocolVersion() {
    return protocolVersion;
  }

  public String getUsername() {
    return username;
  }

  public String getPassword() {
    return password;
  }

}
