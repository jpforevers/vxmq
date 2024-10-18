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
 *
 */

package cloud.wangyongjun.vxmq.service.client;

import cloud.wangyongjun.vxmq.service.msg.MsgToClient;
import io.vertx.core.json.JsonObject;

public class ToClientVerticleMsg {

  private final Type type;
  private final Object payload;

  public ToClientVerticleMsg(Type type, Object payload) {
    this.type = type;
    this.payload = payload;
  }

  public ToClientVerticleMsg(JsonObject jsonObject) {
    this.type = Type.valueOf(jsonObject.getString("type"));
    JsonObject payloadJsonObject = jsonObject.getJsonObject("payload");
    this.payload = switch (type) {
      case CLOSE_MQTT_ENDPOINT -> new CloseMqttEndpointRequest(payloadJsonObject);
      case DISCONNECT ->  new DisconnectRequest(payloadJsonObject);
      case UNDEPLOY_CLIENT_VERTICLE ->  new UndeployClientVerticleRequest(payloadJsonObject);
      case SEND_PUBLISH ->  new MsgToClient(payloadJsonObject);
    };
  }

  public Type getType() {
    return type;
  }

  public Object getPayload() {
    return payload;
  }

  public JsonObject toJson() {
    JsonObject jsonObject = new JsonObject();
    jsonObject.put("type", type.name());
    JsonObject payloadJsonObject = switch (type) {
      case CLOSE_MQTT_ENDPOINT -> ((CloseMqttEndpointRequest) payload).toJson();
      case DISCONNECT -> ((DisconnectRequest) payload).toJson();
      case UNDEPLOY_CLIENT_VERTICLE -> ((UndeployClientVerticleRequest) payload).toJson();
      case SEND_PUBLISH -> ((MsgToClient) payload).toJson();
    };
    jsonObject.put("payload", payloadJsonObject);
    return jsonObject;
  }

  public enum Type {
    CLOSE_MQTT_ENDPOINT, DISCONNECT, UNDEPLOY_CLIENT_VERTICLE, SEND_PUBLISH
  }

}
