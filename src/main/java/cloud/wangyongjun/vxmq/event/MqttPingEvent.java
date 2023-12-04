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

package cloud.wangyongjun.vxmq.event;

import io.vertx.core.json.JsonObject;

public class MqttPingEvent implements MqttEvent {

  private long time;
  private String nodeId;
  private String clientId;

  public MqttPingEvent() {
  }

  public MqttPingEvent(long time, String nodeId, String clientId) {
    this.time = time;
    this.nodeId = nodeId;
    this.clientId = clientId;
  }

  @Override
  public long getTime() {
    return time;
  }

  @Override
  public EventType getEventType() {
    return EventType.EVENT_MQTT_PING;
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
    return jsonObject;
  }

  @Override
  public MqttPingEvent fromJson(JsonObject jsonObject) {
    this.time = jsonObject.getLong("time");
    this.nodeId = jsonObject.getString("nodeId");
    this.clientId = jsonObject.getString("clientId");
    return this;
  }

  @Override
  public String getClientId() {
    return clientId;
  }

}
