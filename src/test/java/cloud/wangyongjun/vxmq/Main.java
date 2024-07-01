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

package cloud.wangyongjun.vxmq;

import cloud.wangyongjun.vxmq.assist.ConsumerUtil;
import io.smallrye.mutiny.Context;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.mqtt.MqttClientOptions;
import io.vertx.mutiny.core.Vertx;
import io.vertx.mutiny.mqtt.MqttClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;

public class Main extends AbstractVerticle {

  private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);

  @Override
  public Uni<Void> asyncStart() {
    return Uni.createFrom().failure(new RuntimeException("dasdas"));
  }

  public static void main(String[] args) {
    Vertx vertx = Vertx.vertx();
    vertx.deployVerticle(new Main())
      .subscribe().with(v -> {}, t -> {
        Uni.createFrom().voidItem().invoke(() -> System.out.println(123)).subscribe().with(x -> {}, Throwable::printStackTrace);
        t.printStackTrace();
      });
  }

}
