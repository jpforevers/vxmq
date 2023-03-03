package cloud.wangyongjun.vxmq.event;

import io.smallrye.mutiny.Uni;
import io.vertx.core.json.JsonObject;
import io.vertx.mutiny.core.eventbus.MessageConsumer;

import java.util.function.Consumer;

public interface EventService {

  Uni<Void> publishEvent(Event event, boolean local);

  Uni<MessageConsumer<JsonObject>> consumerEvent(EventType eventType, Consumer<JsonObject> consumer, boolean local);

}
