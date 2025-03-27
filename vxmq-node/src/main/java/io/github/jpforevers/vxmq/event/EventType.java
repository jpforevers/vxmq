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

package io.github.jpforevers.vxmq.event;

import io.github.jpforevers.vxmq.assist.EBFactory;
import io.github.jpforevers.vxmq.event.mqtt.MqttConnectFailedEvent;
import io.github.jpforevers.vxmq.event.mqtt.MqttConnectedEvent;
import io.github.jpforevers.vxmq.event.mqtt.MqttDisconnectedEvent;
import io.github.jpforevers.vxmq.event.mqtt.MqttEndpointClosedEvent;
import io.github.jpforevers.vxmq.event.mqtt.MqttPingEvent;
import io.github.jpforevers.vxmq.event.mqtt.MqttProtocolErrorEvent;
import io.github.jpforevers.vxmq.event.mqtt.MqttPublishInboundAcceptedEvent;
import io.github.jpforevers.vxmq.event.mqtt.MqttPublishOutboundAckedEvent;
import io.github.jpforevers.vxmq.event.mqtt.MqttSessionTakenOverEvent;
import io.github.jpforevers.vxmq.event.mqtt.MqttSubscribedEvent;
import io.github.jpforevers.vxmq.event.mqtt.MqttUnsubscribedEvent;
import io.vertx.core.json.JsonObject;

public enum EventType {

  NOTHING(EBFactory.EBAddress.EVENT_NOTHING),
  EVENT_MQTT_CONNECTED(EBFactory.EBAddress.EVENT_MQTT_CONNECTED),
  EVENT_MQTT_SESSION_TAKEN_OVER(EBFactory.EBAddress.EVENT_MQTT_SESSION_TAKEN_OVER),
  EVENT_MQTT_CONNECT_FAILED(EBFactory.EBAddress.EVENT_MQTT_CONNECT_FAILED),
  EVENT_MQTT_PROTOCOL_ERROR(EBFactory.EBAddress.EVENT_MQTT_PROTOCOL_ERROR),
  EVENT_MQTT_ENDPOINT_CLOSED(EBFactory.EBAddress.EVENT_MQTT_ENDPOINT_CLOSED),
  EVENT_MQTT_DISCONNECTED(EBFactory.EBAddress.EVENT_MQTT_DISCONNECTED),
  EVENT_MQTT_PING(EBFactory.EBAddress.EVENT_MQTT_PING),
  EVENT_MQTT_SUBSCRIBED(EBFactory.EBAddress.EVENT_MQTT_SUBSCRIBED),
  EVENT_MQTT_UNSUBSCRIBED(EBFactory.EBAddress.EVENT_MQTT_UNSUBSCRIBED),
  EVENT_MQTT_PUBLISH_INBOUND_ACCEPTED(EBFactory.EBAddress.EVENT_MQTT_PUBLISH_INBOUND_ACCEPTED),
  EVENT_MQTT_PUBLISH_OUTBOUND_ACKED(EBFactory.EBAddress.EVENT_MQTT_PUBLISH_OUTBOUND_ACKED),
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
      case EVENT_MQTT_CONNECTED -> new MqttConnectedEvent().fromJson(data);
      case EVENT_MQTT_SESSION_TAKEN_OVER -> new MqttSessionTakenOverEvent().fromJson(data);
      case EVENT_MQTT_CONNECT_FAILED -> new MqttConnectFailedEvent().fromJson(data);
      case EVENT_MQTT_PROTOCOL_ERROR -> new MqttProtocolErrorEvent().fromJson(data);
      case EVENT_MQTT_ENDPOINT_CLOSED -> new MqttEndpointClosedEvent().fromJson(data);
      case EVENT_MQTT_DISCONNECTED -> new MqttDisconnectedEvent().fromJson(data);
      case EVENT_MQTT_PING -> new MqttPingEvent().fromJson(data);
      case EVENT_MQTT_SUBSCRIBED -> new MqttSubscribedEvent().fromJson(data);
      case EVENT_MQTT_UNSUBSCRIBED -> new MqttUnsubscribedEvent().fromJson(data);
      case EVENT_MQTT_PUBLISH_INBOUND_ACCEPTED -> new MqttPublishInboundAcceptedEvent().fromJson(data);
      case EVENT_MQTT_PUBLISH_OUTBOUND_ACKED -> new MqttPublishOutboundAckedEvent().fromJson(data);
    };
  }

}
