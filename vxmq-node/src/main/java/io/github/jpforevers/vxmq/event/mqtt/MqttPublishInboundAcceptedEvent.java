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
    return EventType.EVENT_MQTT_PUBLISH_INBOUND_ACCEPTED;
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
