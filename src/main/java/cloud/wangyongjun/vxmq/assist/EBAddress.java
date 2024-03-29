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

package cloud.wangyongjun.vxmq.assist;

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
