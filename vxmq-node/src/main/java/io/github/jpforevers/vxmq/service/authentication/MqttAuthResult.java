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

package io.github.jpforevers.vxmq.service.authentication;

import io.github.jpforevers.vxmq.assist.ModelConstants;
import io.netty.handler.codec.mqtt.MqttConnectReturnCode;
import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.json.JsonObject;

@DataObject
public class MqttAuthResult {

  private final MqttConnectReturnCode code;
  private final String reason;

  public MqttAuthResult(MqttConnectReturnCode code) {
    this.code = code;
    this.reason = null;
  }

  public MqttAuthResult(MqttConnectReturnCode code, String reason) {
    this.code = code;
    this.reason = reason;
  }

  public MqttAuthResult(JsonObject jsonObject) {
    this.code = MqttConnectReturnCode.valueOf(jsonObject.getInteger(ModelConstants.FIELD_NAME_CODE).byteValue());
    this.reason = jsonObject.getString(ModelConstants.FIELD_NAME_REASON);
  }

  public JsonObject toJson() {
    JsonObject jsonObject = new JsonObject();
    jsonObject.put(ModelConstants.FIELD_NAME_CODE, code.byteValue());
    jsonObject.put(ModelConstants.FIELD_NAME_REASON, reason);
    return jsonObject;
  }

  public MqttConnectReturnCode getCode() {
    return code;
  }

  public String getReason() {
    return reason;
  }

}
