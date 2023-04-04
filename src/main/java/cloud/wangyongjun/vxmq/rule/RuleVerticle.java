/*
 * Copyright 2018-present 王用军
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
