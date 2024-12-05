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

package io.github.jpforevers.vxmq.event;

import io.smallrye.mutiny.Uni;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.ReplyException;
import io.vertx.core.eventbus.ReplyFailure;
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

    // I found a huge performance difference between version 1.6.0 and versions after 1.7.0:
    // On my work computer, version 1.6.0 can run smoothly under 40000 loads, with a memory usage of 1.5G and a CPU usage of 300%, while versions after 1.7.0 are not, after a certain period of time, the memory will be exhausted and the CPU will rise to 800%.
    // After careful verification, I found that it was caused by the deletion of the following code(commit is: https://github.com/jpforevers/vxmq/commit/5ce841078cfbd78e8c6d0642d184ec388cc93495).
    // I have submitted a comment in the related issue of Vert.x: https://github.com/eclipse-vertx/vert.x/issues/5257#issuecomment-2513917472.
    // Before Vert.x resolving this issue, restore the following code.
    for (EventType eventType : EventType.values()) {
      vertx.eventBus().<JsonObject>consumer(eventType.getEbAddress(), message -> LOGGER.trace("Event {} received, nothing need to do", message.body()));
    }

  }

  @Override
  public Uni<Void> publishEvent(Event event) {
    DeliveryOptions deliveryOptions = new DeliveryOptions();
    deliveryOptions.setLocalOnly(event.isLocal());
    return vertx.eventBus().publisher(event.getEventType().getEbAddress(), deliveryOptions).write(event.toJson())
      .onFailure().recoverWithUni(t -> t instanceof ReplyException && ReplyFailure.NO_HANDLERS.equals(((ReplyException)t).failureType()) ? Uni.createFrom().voidItem() : Uni.createFrom().failure(t));
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
