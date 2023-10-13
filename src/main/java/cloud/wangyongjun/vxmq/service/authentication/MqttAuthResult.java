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

package cloud.wangyongjun.vxmq.service.authentication;

import cloud.wangyongjun.vxmq.assist.ModelConstants;
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
