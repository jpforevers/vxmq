package cloud.wangyongjun.vxmq.event;

import io.vertx.core.json.JsonObject;

public interface Event {

  long getTime();

  EventType getEventType();

  String getNodeId();

  boolean isLocal();

  JsonObject toJson();

  Event fromJson(JsonObject jsonObject);

}
