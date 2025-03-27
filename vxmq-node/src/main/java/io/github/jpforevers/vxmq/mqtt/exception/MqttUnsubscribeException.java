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

package io.github.jpforevers.vxmq.mqtt.exception;

import io.vertx.mqtt.MqttException;
import io.vertx.mqtt.messages.codes.MqttUnsubAckReasonCode;

public class MqttUnsubscribeException extends MqttException {

  private MqttUnsubAckReasonCode mqttUnsubAckReasonCode;

  public MqttUnsubscribeException(String message) {
    super(-1, message);
  }

  public MqttUnsubscribeException(MqttUnsubAckReasonCode mqttUnsubAckReasonCode) {
    this(mqttUnsubAckReasonCode, mqttUnsubAckReasonCode.name());
  }

  public MqttUnsubscribeException(MqttUnsubAckReasonCode mqttUnsubAckReasonCode, String reason) {
    super(mqttUnsubAckReasonCode.value(), reason);
    this.mqttUnsubAckReasonCode = mqttUnsubAckReasonCode;
  }

  public MqttUnsubAckReasonCode getMqttUnsubAckReasonCode() {
    return mqttUnsubAckReasonCode;
  }

}
