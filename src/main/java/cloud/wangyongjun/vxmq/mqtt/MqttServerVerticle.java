package cloud.wangyongjun.vxmq.mqtt;

import cloud.wangyongjun.vxmq.assist.Config;
import cloud.wangyongjun.vxmq.assist.ServiceFactory;
import cloud.wangyongjun.vxmq.mqtt.handler.MqttEndpointHandler;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.vertx.core.AbstractVerticle;
import io.vertx.mqtt.MqttServerOptions;
import io.vertx.mutiny.mqtt.MqttServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MqttServerVerticle extends AbstractVerticle {

  private final static Logger LOGGER = LoggerFactory.getLogger(MqttServerVerticle.class);

  @Override
  public Uni<Void> asyncStart() {
    MqttServerOptions mqttServerOptions = new MqttServerOptions().setAutoClientId(false).setPort(Config.getMqttServerPort(config()));
    MqttServer mqttServer = MqttServer.create(vertx, mqttServerOptions);
    mqttServer.endpointHandler(new MqttEndpointHandler(vertx, config(),
      ServiceFactory.sessionService(vertx),
      ServiceFactory.msgService(vertx, config()),
      ServiceFactory.willService(vertx),
      ServiceFactory.clientService(vertx),
      ServiceFactory.subService(vertx),
      ServiceFactory.retainService(vertx),
      ServiceFactory.compositeService(vertx, config()),
      ServiceFactory.eventService(vertx))
    );
    mqttServer.exceptionHandler(t -> LOGGER.error("Error occurred at mqtt server layer", t));
    return mqttServer.listen().replaceWithVoid();
  }

  @Override
  public Uni<Void> asyncStop() {
    return Uni.createFrom().voidItem();
  }

}