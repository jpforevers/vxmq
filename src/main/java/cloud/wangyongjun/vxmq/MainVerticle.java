package cloud.wangyongjun.vxmq;

import cloud.wangyongjun.vxmq.http.HttpServerVerticle;
import cloud.wangyongjun.vxmq.mqtt.MqttServerVerticle;
import cloud.wangyongjun.vxmq.mqtt.sub.SubVerticle;
import cloud.wangyongjun.vxmq.shell.ShellServerVerticle;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.http.impl.HttpServerImpl;
import io.vertx.core.impl.VertxInternal;
import io.vertx.core.impl.cpu.CpuCoreSensor;
import io.vertx.core.net.impl.NetServerImpl;
import io.vertx.core.net.impl.ServerID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.Map;

public class MainVerticle extends AbstractVerticle {

  private static final Logger LOGGER = LoggerFactory.getLogger(MainVerticle.class);

  @Override
  public Uni<Void> asyncStart() {

    return Uni.createFrom().voidItem()
      .onItem().transformToUni(v -> showBanner())
      .onItem().transformToUni(v -> deployVerticle())
      .onItem().transformToUni(v -> printServers())
      .replaceWithVoid();
  }

  private Uni<Void> showBanner() {
    return vertx.fileSystem().readFile("banner.txt")
      .onItem().invoke(buffer -> LOGGER.info(buffer.toString(StandardCharsets.UTF_8)))
      .replaceWithVoid();
  }

  /**
   * Deploy application verticles
   *
   * @return void
   */
  private Uni<Void> deployVerticle() {
    return Uni.createFrom().voidItem()
      .onItem().transformToUni(s -> vertx.deployVerticle(HttpServerVerticle.class.getName(), new DeploymentOptions().setConfig(config()).setInstances(CpuCoreSensor.availableProcessors())))
      .onItem().invoke(s -> LOGGER.info("{} deployed", HttpServerVerticle.class.getSimpleName()))

      .onItem().transformToUni(s -> vertx.deployVerticle(MqttServerVerticle.class.getName(), new DeploymentOptions().setConfig(config()).setInstances(CpuCoreSensor.availableProcessors())))
      .onItem().invoke(s -> LOGGER.info("{} deployed", MqttServerVerticle.class.getSimpleName()))

      .onItem().transformToUni(s -> vertx.deployVerticle(ShellServerVerticle.class.getName(), new DeploymentOptions().setConfig(config())))
      .onItem().invoke(s -> LOGGER.info("{} deployed", ShellServerVerticle.class.getSimpleName()))

      .onItem().transformToUni(s -> vertx.deployVerticle(SubVerticle.class.getName(), new DeploymentOptions().setConfig(config())))
      .onItem().invoke(s -> LOGGER.info("{} deployed", SubVerticle.class.getSimpleName()))

      .replaceWithVoid();
  }

  /**
   * Print application servers
   *
   * @return void
   */
  private Uni<Void> printServers() {
    VertxInternal vertxInternal = (VertxInternal) vertx.getDelegate();
    LOGGER.info("Net Servers:");
    for (Map.Entry<ServerID, NetServerImpl> server : vertxInternal.sharedNetServers().entrySet()) {
      LOGGER.info(server.getKey().host + ":" + server.getKey().port);
    }
    LOGGER.info("HTTP Servers:");
    for (Map.Entry<ServerID, HttpServerImpl> server : vertxInternal.sharedHttpServers().entrySet()) {
      LOGGER.info(server.getKey().host + ":" + server.getKey().port);
    }
    return Uni.createFrom().voidItem();
  }

}
