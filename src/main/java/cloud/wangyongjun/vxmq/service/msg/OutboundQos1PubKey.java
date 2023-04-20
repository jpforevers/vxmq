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
import org.apache.ignite.cache.affinity.AffinityKeyMapped;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
public class OutboundQos1PubKey implements Comparable<OutboundQos1PubKey> {

  @AffinityKeyMapped
  private String sessionId;
  private int messageId;

  public OutboundQos1PubKey() {
  }

  public OutboundQos1PubKey(String sessionId, int messageId) {
    this.sessionId = sessionId;
    this.messageId = messageId;
  }

  public OutboundQos1PubKey(JsonObject jsonObject) {
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
    OutboundQos1PubKey that = (OutboundQos1PubKey) o;
    return messageId == that.messageId && sessionId.equals(that.sessionId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(sessionId, messageId);
  }

  @Override
  public int compareTo(@NotNull OutboundQos1PubKey o) {
    if (!this.sessionId.equals(o.sessionId)) {
      return this.sessionId.compareTo(o.getSessionId());
    } else {
      return Integer.compare(this.messageId, o.messageId);
    }
  }

}
