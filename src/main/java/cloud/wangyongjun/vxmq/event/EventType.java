package cloud.wangyongjun.vxmq.event;

import cloud.wangyongjun.vxmq.assist.EBAddress;

public enum EventType {

  EVENT_NOTHING(EBAddress.EVENT_NOTHING),
  EVENT_MQTT_CONNECTED_EVENT(EBAddress.EVENT_MQTT_CONNECTED_EVENT),
  EVENT_MQTT_ENDPOINT_CLOSED_EVENT(EBAddress.EVENT_MQTT_ENDPOINT_CLOSED_EVENT),
  EVENT_MQTT_DISCONNECTED_EVENT(EBAddress.EVENT_MQTT_DISCONNECTED_EVENT),
  EVENT_MQTT_SUBSCRIBED_EVENT(EBAddress.EVENT_MQTT_SUBSCRIBED_EVENT),
  EVENT_MQTT_UNSUBSCRIBED_EVENT(EBAddress.EVENT_MQTT_UNSUBSCRIBED_EVENT)
  ;

  private final String ebAddress;

  EventType(String ebAddress) {
    this.ebAddress = ebAddress;
  }

  public String getEbAddress() {
    return ebAddress;
  }
}
