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

package cloud.wangyongjun.vxmq.event.mqtt;

import cloud.wangyongjun.vxmq.event.EventType;
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
    return EventType.EVENT_MQTT_DISCONNECTED;
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
    this.sessionId = jsonObject.getString("sessionId");
    this.code = MqttDisconnectReasonCode.valueOf(jsonObject.getString("code"));
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
