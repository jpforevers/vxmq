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

import io.vertx.core.json.JsonObject;

import java.time.Instant;

public class OutboundQos2Rel {

  private String sessionId;
  private String clientId;
  private int messageId;
  private long createdTime;

  public OutboundQos2Rel() {
  }

  public OutboundQos2Rel(String sessionId, String clientId, int messageId, long createdTime) {
    this.sessionId = sessionId;
    this.clientId = clientId;
    this.messageId = messageId;
    this.createdTime = createdTime;
  }

  public OutboundQos2Rel(JsonObject jsonObject) {
    this.sessionId = jsonObject.getString("sessionId");
    this.clientId = jsonObject.getString("clientId");
    this.messageId = jsonObject.getInteger("messageId");
    this.createdTime = Instant.parse(jsonObject.getString("createdTime")).toEpochMilli();
  }

  public JsonObject toJson() {
    JsonObject jsonObject = new JsonObject();
    jsonObject.put("sessionId", this.sessionId);
    jsonObject.put("clientId", this.clientId);
    jsonObject.put("messageId", this.messageId);
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

  public long getCreatedTime() {
    return createdTime;
  }

  public OutboundQos2RelKey getKey() {
    return new OutboundQos2RelKey(sessionId, messageId);
  }

}
