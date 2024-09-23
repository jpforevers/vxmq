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

package cloud.wangyongjun.vxmq;

import cloud.wangyongjun.vxmq.assist.ConsumerUtil;
import cloud.wangyongjun.vxmq.assist.MqttPropertiesUtil;
import io.vertx.core.json.JsonObject;
import io.vertx.mqtt.MqttClientOptions;
import io.vertx.mutiny.core.Vertx;
import io.vertx.mutiny.mqtt.MqttClient;
import io.vertx.mutiny.mqtt.messages.MqttPublishMessage;

import java.time.Instant;

public class Main {

  public static void main(String[] args) {
    Vertx vertx = Vertx.vertx();

    MqttClientOptions mqttClientOptions = new MqttClientOptions();
//    mqttClientOptions.setAutoAck(false);
    mqttClientOptions.setCleanSession(false);
    MqttClient mqttClient = MqttClient.create(vertx, mqttClientOptions);

    mqttClient.publishHandler(mqttPublishMessage -> {
      System.out.println(publicationInfo(mqttPublishMessage));
//      mqttPublishMessage.ack();
    });

    mqttClient.connect(1883, "localhost")
      .onItem().invoke(mqttConnAckMessage -> System.out.println("Connected, code: " + mqttConnAckMessage.code()))
      .onItem().transformToUni(v -> mqttClient.subscribe("abc/def/+", 1))
      .subscribe().with(v -> {}, Throwable::printStackTrace);
  }

  private static String publicationInfo(MqttPublishMessage mqttPublishMessage) {
    JsonObject jsonObject = new JsonObject();
    jsonObject.put("topicName", mqttPublishMessage.topicName());
    jsonObject.put("mqttQoS", mqttPublishMessage.qosLevel());
    jsonObject.put("messageId", mqttPublishMessage.messageId());
    jsonObject.put("payload", mqttPublishMessage.payload().getDelegate());
    jsonObject.put("dup", mqttPublishMessage.isDup());
    jsonObject.put("retain", mqttPublishMessage.isRetain());
    jsonObject.put("properties", MqttPropertiesUtil.encode(mqttPublishMessage.properties()));
    return jsonObject.toString();
  }
}
