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
import io.vertx.core.json.JsonObject;

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
    this.sessionId = jsonObject.getString(ModelConstants.FIELD_NAME_SESSION_ID);
    this.clientId = jsonObject.getString(ModelConstants.FIELD_NAME_CLIENT_ID);
    this.messageId = jsonObject.getInteger(ModelConstants.FIELD_NAME_MESSAGE_ID);
    this.createdTime = jsonObject.getLong(ModelConstants.FIELD_NAME_CREATED_TIME);
  }

  public JsonObject toJson() {
    JsonObject jsonObject = new JsonObject();
    jsonObject.put(ModelConstants.FIELD_NAME_SESSION_ID, this.sessionId);
    jsonObject.put(ModelConstants.FIELD_NAME_CLIENT_ID, this.clientId);
    jsonObject.put(ModelConstants.FIELD_NAME_MESSAGE_ID, this.messageId);
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
