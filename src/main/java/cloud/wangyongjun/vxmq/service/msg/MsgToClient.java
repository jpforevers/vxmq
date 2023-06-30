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

public class MsgToClient {

  private String sessionId;
  private String clientId;
  private Integer messageId;
  private String topic;
  private int qos;
  private Buffer payload;
  private boolean dup;
  private boolean retain;

  private long createdTime;

  public MsgToClient() {
  }

  public MsgToClient(JsonObject jsonObject) {
    this.sessionId = jsonObject.getString(ModelConstants.FIELD_NAME_SESSION_ID);
    this.clientId = jsonObject.getString(ModelConstants.FIELD_NAME_CLIENT_ID);
    this.messageId = jsonObject.getInteger(ModelConstants.FIELD_NAME_MESSAGE_ID);
    this.topic = jsonObject.getString(ModelConstants.FIELD_NAME_TOPIC);
    this.qos = jsonObject.getInteger(ModelConstants.FIELD_NAME_QOS);
    this.payload = jsonObject.getBuffer(ModelConstants.FIELD_NAME_PAYLOAD);
    this.dup = jsonObject.getBoolean(ModelConstants.FIELD_NAME_DUP);
    this.retain = jsonObject.getBoolean(ModelConstants.FIELD_NAME_RETAIN);
    this.createdTime = jsonObject.getLong(ModelConstants.FIELD_NAME_CREATED_TIME);
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
    jsonObject.put(ModelConstants.FIELD_NAME_CREATED_TIME, this.createdTime);
    return jsonObject;
  }

  @Override
  public String toString() {
    return toJson().toString();
  }

  public String getSessionId() {
    return sessionId;
  }

  public MsgToClient setSessionId(String sessionId) {
    this.sessionId = sessionId;
    return this;
  }

  public String getClientId() {
    return clientId;
  }

  public MsgToClient setClientId(String clientId) {
    this.clientId = clientId;
    return this;
  }

  public Integer getMessageId() {
    return messageId;
  }

  public MsgToClient setMessageId(Integer messageId) {
    this.messageId = messageId;
    return this;
  }

  public String getTopic() {
    return topic;
  }

  public MsgToClient setTopic(String topic) {
    this.topic = topic;
    return this;
  }

  public int getQos() {
    return qos;
  }

  public MsgToClient setQos(int qos) {
    this.qos = qos;
    return this;
  }

  public Buffer getPayload() {
    return payload;
  }

  public MsgToClient setPayload(Buffer payload) {
    this.payload = payload;
    return this;
  }

  public boolean isDup() {
    return dup;
  }

  public MsgToClient setDup(boolean dup) {
    this.dup = dup;
    return this;
  }

  public boolean isRetain() {
    return retain;
  }

  public MsgToClient setRetain(boolean retain) {
    this.retain = retain;
    return this;
  }

  public long getCreatedTime() {
    return createdTime;
  }

  public MsgToClient setCreatedTime(long createdTime) {
    this.createdTime = createdTime;
    return this;
  }
}
