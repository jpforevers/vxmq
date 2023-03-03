package cloud.wangyongjun.vxmq.mqtt.sub;

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
    this.sessionId = jsonObject.getString("sessionId");
    this.topicFilter = jsonObject.getString("topicFilter");
  }

  public JsonObject toJson() {
    JsonObject jsonObject = new JsonObject();
    jsonObject.put("sessionId", this.sessionId);
    jsonObject.put("topicFilter", this.topicFilter);
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
