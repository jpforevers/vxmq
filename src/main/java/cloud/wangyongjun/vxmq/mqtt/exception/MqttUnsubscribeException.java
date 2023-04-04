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

package cloud.wangyongjun.vxmq.mqtt.exception;

import io.vertx.mqtt.messages.codes.MqttUnsubAckReasonCode;

public class MqttUnsubscribeException extends MqttException {

  private MqttUnsubAckReasonCode code;

  /**
   * Suitable for MQTT version 3.
   *
   * @param message message
   */
  public MqttUnsubscribeException(String message) {
    super(message);
  }

  /**
   * Suitable for MQTT version above 3.
   *
   * @param code code
   */
  public MqttUnsubscribeException(MqttUnsubAckReasonCode code) {
    super(String.format("Unsubscribe failed: %s", code));
    this.code = code;
  }

  public MqttUnsubAckReasonCode code() {
    return this.code;
  }

}
