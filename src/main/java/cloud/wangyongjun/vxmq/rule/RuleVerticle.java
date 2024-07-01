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

    return Uni.createFrom().voidItem()
      .onItem().transformToUni(v -> {
        if (Config.getRuleStaticWriteMqttEventToMqttEnable(config())) {
          return vertx.deployVerticle(WriteMqttEventToMqttStaticRule.class.getName(), new DeploymentOptions().setConfig(config()))
            .replaceWithVoid();
        } else {
          return Uni.createFrom().voidItem();
        }
      })
      .onItem().transformToUni(v -> {
        if (Config.getRuleStaticWriteMqttEventToKafkaEnable(config())) {
          return vertx.deployVerticle(WriteMqttEventToKafkaStaticRule.class.getName(), new DeploymentOptions().setConfig(config()))
            .replaceWithVoid();
        } else {
          return Uni.createFrom().voidItem();
        }
      })
      .onItem().transformToUni(v -> {
        if (Config.getRuleStaticReadMqttPublishFromKafkaEnable(config())) {
          return vertx.deployVerticle(ReadMqttPublishFromKafkaStaticRule.class.getName(), new DeploymentOptions().setConfig(config()))
            .replaceWithVoid();
        } else {
          return Uni.createFrom().voidItem();
        }
      });
  }

  @Override
  public Uni<Void> asyncStop() {

    return Uni.createFrom().voidItem();
  }

}
