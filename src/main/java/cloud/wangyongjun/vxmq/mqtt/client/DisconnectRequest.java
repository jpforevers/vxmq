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

package cloud.wangyongjun.vxmq.mqtt.client;

import cloud.wangyongjun.vxmq.mqtt.MqttPropertiesUtil;
import io.netty.handler.codec.mqtt.MqttProperties;
import io.vertx.core.json.JsonObject;
import io.vertx.mqtt.messages.codes.MqttDisconnectReasonCode;


public class DisconnectRequest {

  private final MqttDisconnectReasonCode mqttDisconnectReasonCode;
  private final MqttProperties disconnectProperties;

  public DisconnectRequest(MqttDisconnectReasonCode mqttDisconnectReasonCode, MqttProperties disconnectProperties) {
    this.mqttDisconnectReasonCode = mqttDisconnectReasonCode;
    this.disconnectProperties = disconnectProperties;
  }

  public DisconnectRequest(JsonObject jsonObject) {
    this.mqttDisconnectReasonCode = MqttDisconnectReasonCode.valueOf(jsonObject.getString("mqttDisconnectReasonCode"));
    this.disconnectProperties = MqttPropertiesUtil.decode(jsonObject.getJsonArray("disconnectProperties"));
  }

  public JsonObject toJson() {
    JsonObject jsonObject = new JsonObject();
    jsonObject.put("mqttDisconnectReasonCode", mqttDisconnectReasonCode.value());
    jsonObject.put("disconnectProperties", MqttPropertiesUtil.encode(disconnectProperties));
    return jsonObject;
  }

  public MqttDisconnectReasonCode getMqttDisconnectReasonCode() {
    return mqttDisconnectReasonCode;
  }

  public MqttProperties getDisconnectProperties() {
    return disconnectProperties;
  }

}
