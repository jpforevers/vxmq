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

package cloud.wangyongjun.vxmq.mqtt.handler;

import io.vertx.mutiny.mqtt.MqttEndpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Consumer;

/**
 * This will be called when an error at protocol level happens
 */
public class MqttExceptionHandler implements Consumer<Throwable> {

  private final static Logger LOGGER = LoggerFactory.getLogger(MqttExceptionHandler.class);

  private final MqttEndpoint mqttEndpoint;

  public MqttExceptionHandler(MqttEndpoint mqttEndpoint) {
    this.mqttEndpoint = mqttEndpoint;
  }

  @Override
  public void accept(Throwable throwable) {
    LOGGER.error("Error occurred at protocol level of " + mqttEndpoint.clientIdentifier(), throwable);
  }

}
