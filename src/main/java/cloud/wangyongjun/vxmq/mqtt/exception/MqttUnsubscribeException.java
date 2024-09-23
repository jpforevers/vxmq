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
