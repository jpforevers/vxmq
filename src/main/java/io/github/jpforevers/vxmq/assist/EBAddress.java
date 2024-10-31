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

package io.github.jpforevers.vxmq.assist;

public class EBAddress {

  public static final String SERVICE_NOTHING_SERVICE = "service.NothingService";
  public static final String SERVICE_SUB_SERVICE = "service.SubService";
  public static final String SERVICE_AUTHENTICATION_SERVICE = "service.AuthenticationService";

  public static final String EVENT_NOTHING = "event.nothing";
  public static final String EVENT_MQTT_CONNECTED = "event.mqtt.connected";
  public static final String EVENT_MQTT_SESSION_TAKEN_OVER = "event.mqtt.session.taken-over";
  public static final String EVENT_MQTT_CONNECT_FAILED = "event.mqtt.connect.failed";
  public static final String EVENT_MQTT_PROTOCOL_ERROR = "event.mqtt.protocol.error";
  public static final String EVENT_MQTT_ENDPOINT_CLOSED = "event.mqtt.endpoint-closed";
  public static final String EVENT_MQTT_DISCONNECTED = "event.mqtt.disconnected";
  public static final String EVENT_MQTT_PING = "event.mqtt.ping";
  public static final String EVENT_MQTT_SUBSCRIBED = "event.mqtt.subscribed";
  public static final String EVENT_MQTT_UNSUBSCRIBED = "event.mqtt.unsubscribed";
  public static final String EVENT_MQTT_PUBLISH_INBOUND_ACCEPTED = "event.mqtt.publish.inbound.accepted";
  public static final String EVENT_MQTT_PUBLISH_OUTBOUND_ACKED = "event.mqtt.publish.outbound.acked";

}
