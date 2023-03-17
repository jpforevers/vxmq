package cloud.wangyongjun.vxmq.mqtt.handler;

import cloud.wangyongjun.vxmq.assist.ConsumerUtil;
import cloud.wangyongjun.vxmq.assist.VertxUtil;
import cloud.wangyongjun.vxmq.event.EventService;
import cloud.wangyongjun.vxmq.event.MqttPingEvent;
import cloud.wangyongjun.vxmq.mqtt.session.SessionService;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.core.Vertx;
import io.vertx.mutiny.mqtt.MqttEndpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;

/**
 * This handler is called when a PINGREQ message is received by the remote MQTT client. In any case the endpoint sends the PINGRESP internally after executing this handler.
 */
public class MqttPingHandler implements Runnable {

  private final static Logger LOGGER = LoggerFactory.getLogger(MqttPingHandler.class);

  private final MqttEndpoint mqttEndpoint;
  private final Vertx vertx;
  private final SessionService sessionService;
  private final EventService eventService;

  public MqttPingHandler(MqttEndpoint mqttEndpoint, Vertx vertx, SessionService sessionService, EventService eventService) {
    this.mqttEndpoint = mqttEndpoint;
    this.vertx = vertx;
    this.sessionService = sessionService;
    this.eventService = eventService;
  }

  @Override
  public void run() {
    LOGGER.debug("PINGREQ from {}", mqttEndpoint.clientIdentifier());
    String clientId = mqttEndpoint.clientIdentifier();
    Uni.createFrom().voidItem()
      .onItem().transformToUni(v -> sessionService.updateLatestUpdatedTime(clientId, Instant.now().toEpochMilli()))
      .onItem().transformToUni(v -> eventService.publishEvent(new MqttPingEvent(Instant.now().toEpochMilli(), VertxUtil.getNodeId(vertx), clientId)))
      .subscribe().with(ConsumerUtil.nothingToDo(), t -> LOGGER.error("Error occurred when processing the PINGREQ from " + clientId, t));
  }

}
