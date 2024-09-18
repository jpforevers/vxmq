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

package cloud.wangyongjun.vxmq.service.client;

import cloud.wangyongjun.vxmq.assist.ModelConstants;
import cloud.wangyongjun.vxmq.assist.MqttPropertiesUtil;
import io.netty.handler.codec.mqtt.MqttProperties;
import io.vertx.core.json.JsonObject;
import io.vertx.mqtt.messages.codes.MqttDisconnectReasonCode;


public class DisconnectRequest {

  private final MqttDisconnectReasonCode mqttDisconnectReasonCode;
  private final MqttProperties mqttProperties;

  public DisconnectRequest(MqttDisconnectReasonCode mqttDisconnectReasonCode, MqttProperties mqttProperties) {
    this.mqttDisconnectReasonCode = mqttDisconnectReasonCode;
    this.mqttProperties = mqttProperties;
  }

  public DisconnectRequest(JsonObject jsonObject) {
    this.mqttDisconnectReasonCode = MqttDisconnectReasonCode.valueOf(jsonObject.getInteger(ModelConstants.FIELD_NAME_MQTT_DISCONNECT_REASON_CODE).byteValue());
    this.mqttProperties = MqttPropertiesUtil.decode(jsonObject.getJsonArray(ModelConstants.FIELD_NAME_MQTT_PROPERTIES));
  }

  public JsonObject toJson() {
    JsonObject jsonObject = new JsonObject();
    jsonObject.put(ModelConstants.FIELD_NAME_MQTT_DISCONNECT_REASON_CODE, mqttDisconnectReasonCode.value());
    jsonObject.put(ModelConstants.FIELD_NAME_MQTT_PROPERTIES, MqttPropertiesUtil.encode(mqttProperties));
    return jsonObject;
  }

  public MqttDisconnectReasonCode getMqttDisconnectReasonCode() {
    return mqttDisconnectReasonCode;
  }

  public MqttProperties getMqttProperties() {
    return mqttProperties;
  }

}
