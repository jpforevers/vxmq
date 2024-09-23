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

import io.netty.handler.codec.mqtt.MqttConnectReturnCode;
import org.apache.commons.lang3.StringUtils;

public class MqttAuthFailedException extends MqttException {

  private final MqttConnectReturnCode code;
  private final String reason;

  public MqttAuthFailedException(MqttConnectReturnCode code) {
    this(code, code.name());
  }

  public MqttAuthFailedException(MqttConnectReturnCode code, String reason) {
    super(StringUtils.isBlank(reason) ? code.name() : reason);
    this.code = code;
    this.reason = reason;
  }

  public MqttConnectReturnCode getCode() {
    return code;
  }

  public String getReason() {
    return reason;
  }

}
