package cloud.wangyongjun.vxmq.event;

import cloud.wangyongjun.vxmq.assist.EBAddress;
import io.vertx.core.json.JsonObject;

public enum EventType {

  NOTHING(EBAddress.EVENT_NOTHING),
  MQTT_CONNECTED_EVENT(EBAddress.EVENT_MQTT_CONNECTED),
  MQTT_ENDPOINT_CLOSED_EVENT(EBAddress.EVENT_MQTT_ENDPOINT_CLOSED),
  MQTT_DISCONNECTED_EVENT(EBAddress.EVENT_MQTT_DISCONNECTED),
  MQTT_PING_EVENT(EBAddress.EVENT_MQTT_PING),
  MQTT_SUBSCRIBED_EVENT(EBAddress.EVENT_MQTT_SUBSCRIBED),
  MQTT_UNSUBSCRIBED_EVENT(EBAddress.EVENT_MQTT_UNSUBSCRIBED),
  MQTT_PUBLISH_INBOUND_ACCEPTED_EVENT(EBAddress.EVENT_MQTT_PUBLISH_INBOUND_ACCEPTED)
  ;

  private final String ebAddress;

  EventType(String ebAddress) {
    this.ebAddress = ebAddress;
  }

  public String getEbAddress() {
    return ebAddress;
  }

  public Event fromJson(JsonObject data){
    return switch (this){
      case NOTHING -> null;
      case MQTT_CONNECTED_EVENT -> new MqttConnectedEvent().fromJson(data);
      case MQTT_ENDPOINT_CLOSED_EVENT -> new MqttEndpointClosedEvent().fromJson(data);
      case MQTT_DISCONNECTED_EVENT -> new MqttDisconnectedEvent().fromJson(data);
      case MQTT_PING_EVENT -> new MqttPingEvent().fromJson(data);
      case MQTT_SUBSCRIBED_EVENT -> new MqttSubscribedEvent().fromJson(data);
      case MQTT_UNSUBSCRIBED_EVENT -> new MqttUnsubscribedEvent().fromJson(data);
      case MQTT_PUBLISH_INBOUND_ACCEPTED_EVENT -> new MqttPublishInboundAcceptedEvent().fromJson(data);
    };
  }

}
