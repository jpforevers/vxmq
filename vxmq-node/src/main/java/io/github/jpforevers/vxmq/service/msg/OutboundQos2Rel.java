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

package io.github.jpforevers.vxmq.service.msg;

import io.github.jpforevers.vxmq.assist.ModelConstants;
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
