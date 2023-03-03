package cloud.wangyongjun.vxmq.mqtt.msg;

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
    this.sessionId = jsonObject.getString("sessionId");
    this.messageId = jsonObject.getInteger("messageId");
  }

  public JsonObject toJson() {
    JsonObject jsonObject = new JsonObject();
    jsonObject.put("sessionId", sessionId);
    jsonObject.put("messageId", messageId);
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
