package cloud.wangyongjun.vxmq.event;

import io.smallrye.mutiny.Uni;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.mutiny.core.Vertx;
import io.vertx.mutiny.core.eventbus.MessageConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Consumer;

public class DefaultEventService implements EventService {

  private final static Logger LOGGER = LoggerFactory.getLogger(DefaultEventService.class);

  private static volatile DefaultEventService defaultEventService;

  public static DefaultEventService getSingleton(Vertx vertx) {
    if (defaultEventService == null) {
      synchronized (DefaultEventService.class) {
        if (defaultEventService == null) {
          defaultEventService = new DefaultEventService(vertx);
        }
      }
    }
    return defaultEventService;
  }

  private final Vertx vertx;

  private DefaultEventService(Vertx vertx) {
    this.vertx = vertx;

    // If you publish a message to a vertx event bus address which no handler consumer it, will cause an exception "No handlers for address xxx".
    // Thus, I register an empty handler for every event address to avoid this exception.
    for (EventType eventType : EventType.values()) {
      vertx.eventBus().<JsonObject>consumer(eventType.getEbAddress(), message -> LOGGER.trace("Event {} received, nothing need to do", message.body()));
    }
  }

  @Override
  public Uni<Void> publishEvent(Event event) {
    vertx.eventBus().publish(event.getEventType().getEbAddress(), event.toJson(), new DeliveryOptions());
    return Uni.createFrom().voidItem();
  }

  @Override
  public Uni<MessageConsumer<JsonObject>> consumerEvent(EventType eventType, Consumer<JsonObject> consumer) {
    MessageConsumer<JsonObject> messageConsumer = vertx.eventBus().consumer(eventType.getEbAddress(), message -> consumer.accept(message.body()));
    return messageConsumer.completionHandler().replaceWith(messageConsumer);
  }

}
