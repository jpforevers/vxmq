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

package cloud.wangyongjun.vxmq.mqtt.msg;

import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;

import java.time.Instant;

public class OutboundQos2Pub {

  private String sessionId;
  private String clientId;
  private int messageId;
  private String topic;
  private int qos;
  private Buffer payload;
  private boolean dup;
  private boolean retain;
  private long createdTime;

  public OutboundQos2Pub() {
  }

  public OutboundQos2Pub(String sessionId, String clientId, int messageId, String topic, int qos, Buffer payload, boolean dup, boolean retain, long createdTime) {
    this.sessionId = sessionId;
    this.clientId = clientId;
    this.messageId = messageId;
    this.topic = topic;
    this.qos = qos;
    this.payload = payload;
    this.dup = dup;
    this.retain = retain;
    this.createdTime = createdTime;
  }

  public OutboundQos2Pub(JsonObject jsonObject) {
    this.sessionId = jsonObject.getString("sessionId");
    this.clientId = jsonObject.getString("clientId");
    this.messageId = jsonObject.getInteger("messageId");
    this.topic = jsonObject.getString("topic");
    this.qos = jsonObject.getInteger("qos");
    this.payload = jsonObject.getBuffer("payload");
    this.dup = jsonObject.getBoolean("dup");
    this.retain = jsonObject.getBoolean("retain");
    this.createdTime = Instant.parse(jsonObject.getString("createdTime")).toEpochMilli();
  }

  public JsonObject toJson() {
    JsonObject jsonObject = new JsonObject();
    jsonObject.put("sessionId", this.sessionId);
    jsonObject.put("clientId", this.clientId);
    jsonObject.put("messageId", this.messageId);
    jsonObject.put("topic", this.topic);
    jsonObject.put("qos", this.qos);
    jsonObject.put("payload", this.payload);
    jsonObject.put("dup", this.dup);
    jsonObject.put("retain", this.retain);
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

  public int getMessageId() {
    return messageId;
  }

  public String getTopic() {
    return topic;
  }

  public int getQos() {
    return qos;
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

  public long getCreatedTime() {
    return createdTime;
  }

  public OutboundQos2PubKey getKey() {
    return new OutboundQos2PubKey(sessionId, messageId);
  }

}
