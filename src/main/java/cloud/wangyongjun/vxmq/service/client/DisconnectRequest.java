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
