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

import io.vertx.mqtt.MqttException;
import io.vertx.mqtt.messages.codes.MqttSubAckReasonCode;

public class MqttSubscribeException extends MqttException {

  private final MqttSubAckReasonCode mqttSubAckReasonCode;

  public MqttSubscribeException(MqttSubAckReasonCode mqttSubAckReasonCode) {
    this(mqttSubAckReasonCode, mqttSubAckReasonCode.name());
  }

  public MqttSubscribeException(MqttSubAckReasonCode mqttSubAckReasonCode, String reason) {
    super(mqttSubAckReasonCode.value(), reason);
    this.mqttSubAckReasonCode = mqttSubAckReasonCode;
  }

  public MqttSubAckReasonCode getMqttSubAckReasonCode() {
    return mqttSubAckReasonCode;
  }

}
