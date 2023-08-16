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
import io.vertx.mqtt.MqttClientOptions;
import io.vertx.mutiny.core.Vertx;
import io.vertx.mutiny.mqtt.MqttClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;

public class Main {

  private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);

  public static void main(String[] args) {
    Vertx vertx = Vertx.vertx();
    MqttClient mqttClient1 = MqttClient.create(vertx, new MqttClientOptions().setClientId("d1"));
    MqttClient mqttClient2 = MqttClient.create(vertx, new MqttClientOptions().setClientId("d1"));
    mqttClient1.connect(1884, "localhost")
      .onItem().invoke(mqttConnAckMessage -> System.out.println("client1: " + mqttConnAckMessage.code()))
      .subscribe().with(v -> {}, Throwable::printStackTrace);
    mqttClient2.connect(1884, "localhost")
      .onItem().invoke(mqttConnAckMessage -> System.out.println("client2: " + mqttConnAckMessage.code()))
      .subscribe().with(v -> {}, Throwable::printStackTrace);
  }

}
