package cloud.wangyongjun.vxmq.rule;

import cloud.wangyongjun.vxmq.assist.ConsumerUtil;
import cloud.wangyongjun.vxmq.service.ServiceFactory;
import cloud.wangyongjun.vxmq.event.Event;
import cloud.wangyongjun.vxmq.event.EventService;
import cloud.wangyongjun.vxmq.event.EventType;
import cloud.wangyongjun.vxmq.event.mqtt.MqttEvent;
import cloud.wangyongjun.vxmq.service.composite.CompositeService;
import cloud.wangyongjun.vxmq.service.msg.MsgToTopic;
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
