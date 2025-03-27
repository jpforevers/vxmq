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
import io.vertx.mqtt.messages.codes.MqttPubAckReasonCode;
import io.vertx.mqtt.messages.codes.MqttPubRecReasonCode;

public class MqttPublishException extends MqttException {

  private MqttPubAckReasonCode mqttPubAckReasonCode;
  private MqttPubRecReasonCode mqttPubRecReasonCode;

  public MqttPublishException(String reason) {
    super(-1, reason);
  }

  public MqttPublishException(MqttPubAckReasonCode code) {
    this(code, code.name());
  }

  public MqttPublishException(MqttPubAckReasonCode code, String reason) {
    super(code.value(), reason);
    this.mqttPubAckReasonCode = code;
  }

  public MqttPublishException(MqttPubRecReasonCode code) {
    this(code, code.name());
  }

  public MqttPublishException(MqttPubRecReasonCode code, String reason) {
    super(code.value(), reason);
    this.mqttPubRecReasonCode = code;
  }

  public MqttPubAckReasonCode getMqttPubAckReasonCode() {
    return mqttPubAckReasonCode;
  }

  public MqttPubRecReasonCode getMqttPubRecReasonCode() {
    return mqttPubRecReasonCode;
  }

}
