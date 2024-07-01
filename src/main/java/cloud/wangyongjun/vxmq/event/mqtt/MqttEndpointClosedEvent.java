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

package cloud.wangyongjun.vxmq.event.mqtt;

import cloud.wangyongjun.vxmq.event.EventType;
import io.vertx.core.json.JsonObject;

public class MqttEndpointClosedEvent implements MqttEvent{

  private long time;
  private String nodeId;
  private String clientId;
  private String sessionId;

  public MqttEndpointClosedEvent() {
  }

  public MqttEndpointClosedEvent(long time, String nodeId, String clientId, String sessionId) {
    this.time = time;
    this.nodeId = nodeId;
    this.clientId = clientId;
    this.sessionId = sessionId;
  }

  @Override
  public long getTime() {
    return time;
  }

  @Override
  public EventType getEventType() {
    return EventType.EVENT_MQTT_ENDPOINT_CLOSED;
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
    return jsonObject;
  }

  @Override
  public MqttEndpointClosedEvent fromJson(JsonObject jsonObject) {
    this.time = jsonObject.getLong("time");
    this.clientId = jsonObject.getString("clientId");
    this.nodeId = jsonObject.getString("nodeId");
    this.sessionId = jsonObject.getString("sessionId");
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

  public String getSessionId() {
    return sessionId;
  }
}
