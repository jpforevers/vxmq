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
  }

  @Override
  public Uni<Void> publishEvent(Event event) {
    DeliveryOptions deliveryOptions = new DeliveryOptions();
    deliveryOptions.setLocalOnly(event.isLocal());
    vertx.eventBus().publish(event.getEventType().getEbAddress(), event.toJson(), deliveryOptions);
    return Uni.createFrom().voidItem();
  }

  @Override
  public Uni<MessageConsumer<JsonObject>> consumeEvent(EventType eventType, Consumer<JsonObject> consumer, boolean local) {
    MessageConsumer<JsonObject> messageConsumer;
    if (local){
      messageConsumer = vertx.eventBus().localConsumer(eventType.getEbAddress(), message -> consumer.accept(message.body()));
    }else {
      messageConsumer = vertx.eventBus().consumer(eventType.getEbAddress(), message -> consumer.accept(message.body()));
    }
    return messageConsumer.completionHandler().replaceWith(messageConsumer);
  }

}
