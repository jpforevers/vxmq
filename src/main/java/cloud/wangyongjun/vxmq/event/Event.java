package cloud.wangyongjun.vxmq.event;

import io.vertx.core.json.JsonObject;

public interface Event {

  long getTime();

  EventType getEventType();

  JsonObject toJson();

  Event fromJson(JsonObject jsonObject);

  String getNodeId();

}
