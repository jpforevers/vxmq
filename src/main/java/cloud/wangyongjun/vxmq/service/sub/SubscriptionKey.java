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

package cloud.wangyongjun.vxmq.service.sub;

import cloud.wangyongjun.vxmq.assist.ModelConstants;
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
