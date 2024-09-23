/*
 * Copyright (C) 2023-2024 王用军
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
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
