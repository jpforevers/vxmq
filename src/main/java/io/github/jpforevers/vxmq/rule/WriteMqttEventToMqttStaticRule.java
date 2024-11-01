package io.github.jpforevers.vxmq.rule;

import io.github.jpforevers.vxmq.assist.ConsumerUtil;
import io.github.jpforevers.vxmq.service.ServiceFactory;
import io.github.jpforevers.vxmq.event.Event;
import io.github.jpforevers.vxmq.event.EventService;
import io.github.jpforevers.vxmq.event.EventType;
import io.github.jpforevers.vxmq.event.mqtt.MqttEvent;
import io.github.jpforevers.vxmq.service.composite.CompositeService;
import io.github.jpforevers.vxmq.service.msg.MsgToTopic;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.vertx.core.AbstractVerticle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class WriteMqttEventToMqttStaticRule extends AbstractVerticle {

  private static final Logger LOGGER = LoggerFactory.getLogger(WriteMqttEventToMqttStaticRule.class);

  @Override
  public Uni<Void> asyncStart() {
    EventService eventService = ServiceFactory.eventService(vertx);
    CompositeService compositeService = ServiceFactory.compositeService(vertx);

    List<Uni<Void>> consumeEventUnis = new ArrayList<>();
    for (EventType value : EventType.values()) {
      Uni<Void> consumeEventUni = eventService.consumeEvent(value, data -> {
        if (LOGGER.isDebugEnabled()) {
          LOGGER.debug("Event consumed: {}", data);
        }
        Event event = value.fromJson(data);
        if (event instanceof MqttEvent) {
          MsgToTopic msgToTopic = new MsgToTopic();
          msgToTopic.setTopic("$SYS/" + event.getEventType().getEbAddress().replace('.', '/'));
          msgToTopic.setClientId("vxmq.rule.static.WriteMqttEventToMqtt");
          msgToTopic.setQos(0);
          msgToTopic.setRetain(false);
          msgToTopic.setPayload(event.toJson().toBuffer());
          // From MQTT 5 specification: 1 (0x01) Byte Indicates that the Payload is UTF-8 Encoded Character Data.
          msgToTopic.setPayloadFormatIndicator(1);
          compositeService.forward(msgToTopic)
            .subscribe().with(ConsumerUtil.nothingToDo(), t -> LOGGER.error("Error occurred when write mqtt event to mqtt", t));
        }
      }, true).replaceWithVoid();
      consumeEventUnis.add(consumeEventUni);
    }

    return Uni.createFrom().voidItem()
      .onItem().transformToUni(v -> Uni.combine().all().unis(consumeEventUnis).collectFailures().discardItems());
  }

  @Override
  public Uni<Void> asyncStop() {
    return Uni.createFrom().voidItem();
  }

}
