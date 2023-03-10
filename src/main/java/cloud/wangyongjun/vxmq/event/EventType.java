package cloud.wangyongjun.vxmq.event;

import cloud.wangyongjun.vxmq.assist.EBAddress;

public enum EventType {

  NOTHING(EBAddress.EVENT_NOTHING),
  MQTT_CONNECTED_EVENT(EBAddress.EVENT_MQTT_CONNECTED),
  MQTT_ENDPOINT_CLOSED_EVENT(EBAddress.EVENT_MQTT_ENDPOINT_CLOSED),
  MQTT_DISCONNECTED_EVENT(EBAddress.EVENT_MQTT_DISCONNECTED),
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
}
