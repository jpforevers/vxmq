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

package cloud.wangyongjun.vxmq.service.msg;

import cloud.wangyongjun.vxmq.assist.ModelConstants;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;

import java.time.Instant;

public class OutboundQos1Pub {

  private String sessionId;
  private String clientId;
  private int messageId;
  private String topic;
  private int qos;
  private Buffer payload;
  private boolean dup;
  private boolean retain;
  private long createdTime;

  public OutboundQos1Pub() {
  }

  public OutboundQos1Pub(String sessionId, String clientId, int messageId, String topic, int qos, Buffer payload, boolean dup, boolean retain, long createdTime) {
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

  public OutboundQos1Pub(JsonObject jsonObject) {
    this.sessionId = jsonObject.getString(ModelConstants.FIELD_NAME_SESSION_ID);
    this.clientId = jsonObject.getString(ModelConstants.FIELD_NAME_CLIENT_ID);
    this.messageId = jsonObject.getInteger(ModelConstants.FIELD_NAME_MESSAGE_ID);
    this.topic = jsonObject.getString(ModelConstants.FIELD_NAME_TOPIC);
    this.qos = jsonObject.getInteger(ModelConstants.FIELD_NAME_QOS);
    this.payload = jsonObject.getBuffer(ModelConstants.FIELD_NAME_PAYLOAD);
    this.dup = jsonObject.getBoolean(ModelConstants.FIELD_NAME_DUP);
    this.retain = jsonObject.getBoolean(ModelConstants.FIELD_NAME_RETAIN);
    this.createdTime = Instant.parse(jsonObject.getString(ModelConstants.FIELD_NAME_CREATED_TIME)).toEpochMilli();
  }

  public JsonObject toJson() {
    JsonObject jsonObject = new JsonObject();
    jsonObject.put(ModelConstants.FIELD_NAME_SESSION_ID, this.sessionId);
    jsonObject.put(ModelConstants.FIELD_NAME_CLIENT_ID, this.clientId);
    jsonObject.put(ModelConstants.FIELD_NAME_MESSAGE_ID, this.messageId);
    jsonObject.put(ModelConstants.FIELD_NAME_TOPIC, this.topic);
    jsonObject.put(ModelConstants.FIELD_NAME_QOS, this.qos);
    jsonObject.put(ModelConstants.FIELD_NAME_PAYLOAD, this.payload);
    jsonObject.put(ModelConstants.FIELD_NAME_DUP, this.dup);
    jsonObject.put(ModelConstants.FIELD_NAME_RETAIN, this.retain);
    jsonObject.put(ModelConstants.FIELD_NAME_CREATED_TIME, Instant.ofEpochMilli(this.createdTime).toString());
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

  public OutboundQos1PubKey getKey() {
    return new OutboundQos1PubKey(sessionId, messageId);
  }

}
