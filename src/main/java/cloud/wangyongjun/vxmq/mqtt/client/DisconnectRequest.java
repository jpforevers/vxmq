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
