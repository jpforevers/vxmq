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

import io.vertx.mqtt.messages.codes.MqttPubAckReasonCode;
import io.vertx.mqtt.messages.codes.MqttPubRecReasonCode;

public class MqttPublishException extends MqttException {

  private MqttPubAckReasonCode mqttPubAckReasonCode;
  private MqttPubRecReasonCode mqttPubRecReasonCode;

  /**
   * Suitable for MQTT version 3 and above.
   *
   * @param message message
   */
  public MqttPublishException(String message) {
    super(message);
  }

  /**
   * Suitable for MQTT version above 3.
   *
   * @param code code
   */
  public MqttPublishException(MqttPubAckReasonCode code) {
    super(String.format("Publish failed: %s", code));
    this.mqttPubAckReasonCode = code;
  }

  /**
   * Suitable for MQTT version above 3.
   *
   * @param code code
   */
  public MqttPublishException(MqttPubRecReasonCode code) {
    super(String.format("Publish failed: %s", code));
    this.mqttPubRecReasonCode = code;
  }

  public MqttPubAckReasonCode mqttPubAckReasonCode() {
    return mqttPubAckReasonCode;
  }

  public MqttPubRecReasonCode mqttPubRecReasonCode() {
    return mqttPubRecReasonCode;
  }

}
