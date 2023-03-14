package cloud.wangyongjun.vxmq.rule;

import cloud.wangyongjun.vxmq.assist.Config;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RuleVerticle extends AbstractVerticle {

  private static final Logger LOGGER = LoggerFactory.getLogger(RuleVerticle.class);

  @Override
  public Uni<Void> asyncStart() {
    if (Config.getRuleStaticWriteMqttEventToKafkaEnable(config())) {
      vertx.setTimer(2000, l -> vertx.deployVerticleAndForget(WriteMqttEventToKafkaStaticRule.class.getName(), new DeploymentOptions().setConfig(config())));
    }
    if (Config.getRuleStaticReadMqttPublishFromKafkaEnable(config())) {
      vertx.setTimer(2000, l -> vertx.deployVerticleAndForget(ReadMqttPublishFromKafkaStaticRule.class.getName(), new DeploymentOptions().setConfig(config())));
    }
    return Uni.createFrom().voidItem();
  }

  @Override
  public Uni<Void> asyncStop() {

    return Uni.createFrom().voidItem();
  }

}
