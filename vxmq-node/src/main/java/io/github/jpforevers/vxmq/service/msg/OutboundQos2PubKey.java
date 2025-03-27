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
import org.apache.ignite.cache.affinity.AffinityKeyMapped;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class OutboundQos2PubKey implements Comparable<OutboundQos2PubKey> {

  @AffinityKeyMapped
  private String sessionId;
  private int messageId;

  public OutboundQos2PubKey() {
  }

  public OutboundQos2PubKey(String sessionId, int messageId) {
    this.sessionId = sessionId;
    this.messageId = messageId;
  }

  public OutboundQos2PubKey(JsonObject jsonObject) {
    this.sessionId = jsonObject.getString(ModelConstants.FIELD_NAME_SESSION_ID);
    this.messageId = jsonObject.getInteger(ModelConstants.FIELD_NAME_MESSAGE_ID);
  }

  public JsonObject toJson() {
    JsonObject jsonObject = new JsonObject();
    jsonObject.put(ModelConstants.FIELD_NAME_SESSION_ID, sessionId);
    jsonObject.put(ModelConstants.FIELD_NAME_MESSAGE_ID, messageId);
    return jsonObject;
  }

  @Override
  public String toString() {
    return toJson().toString();
  }

  public String getSessionId() {
    return sessionId;
  }

  public int getMessageId() {
    return messageId;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    OutboundQos2PubKey that = (OutboundQos2PubKey) o;
    return messageId == that.messageId && sessionId.equals(that.sessionId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(sessionId, messageId);
  }

  @Override
  public int compareTo(@NotNull OutboundQos2PubKey o) {
    if (!this.sessionId.equals(o.sessionId)) {
      return this.sessionId.compareTo(o.getSessionId());
    } else {
      return Integer.compare(this.messageId, o.messageId);
    }
  }

}
