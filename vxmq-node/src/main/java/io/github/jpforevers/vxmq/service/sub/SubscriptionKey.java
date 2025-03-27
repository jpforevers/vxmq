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

package io.github.jpforevers.vxmq.service.sub;

import io.github.jpforevers.vxmq.assist.ModelConstants;
import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.json.JsonObject;
import org.apache.ignite.cache.affinity.AffinityKeyMapped;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

@DataObject
public class SubscriptionKey implements Comparable<SubscriptionKey> {

  private String sessionId;
  @AffinityKeyMapped
  private String topicFilter;

  public SubscriptionKey() {
  }

  public SubscriptionKey(String sessionId, String topicFilter) {
    this.sessionId = sessionId;
    this.topicFilter = topicFilter;
  }

  public SubscriptionKey(JsonObject jsonObject) {
    this.sessionId = jsonObject.getString(ModelConstants.FIELD_NAME_SESSION_ID);
    this.topicFilter = jsonObject.getString(ModelConstants.FIELD_NAME_TOPIC_FILTER);
  }

  public JsonObject toJson() {
    JsonObject jsonObject = new JsonObject();
    jsonObject.put(ModelConstants.FIELD_NAME_SESSION_ID, this.sessionId);
    jsonObject.put(ModelConstants.FIELD_NAME_TOPIC_FILTER, this.topicFilter);
    return jsonObject;
  }

  @Override
  public String toString() {
    return toJson().toString();
  }

  public String getSessionId() {
    return sessionId;
  }

  public String getTopicFilter() {
    return topicFilter;
  }

  @Override
  public int compareTo(@NotNull SubscriptionKey o) {
    if (!this.getSessionId().equals(o.getSessionId())) {
      return this.getSessionId().compareTo(o.getSessionId());
    } else {
      return this.getTopicFilter().compareTo(o.getTopicFilter());
    }
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    SubscriptionKey that = (SubscriptionKey) o;
    return sessionId.equals(that.sessionId) && topicFilter.equals(that.topicFilter);
  }

  @Override
  public int hashCode() {
    return Objects.hash(sessionId, topicFilter);
  }

}
