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

package cloud.wangyongjun.vxmq.mqtt.handler;

import cloud.wangyongjun.vxmq.assist.ConsumerUtil;
import cloud.wangyongjun.vxmq.assist.MqttPropertiesUtil;
import cloud.wangyongjun.vxmq.service.composite.CompositeService;
import cloud.wangyongjun.vxmq.service.msg.MsgService;
import cloud.wangyongjun.vxmq.service.msg.MsgToTopic;
import cloud.wangyongjun.vxmq.service.session.SessionService;
import io.netty.handler.codec.mqtt.MqttProperties;
import io.netty.handler.codec.mqtt.MqttVersion;
import io.smallrye.mutiny.Uni;
import io.vertx.core.json.JsonObject;
import io.vertx.mqtt.messages.codes.MqttPubCompReasonCode;
import io.vertx.mutiny.mqtt.MqttEndpoint;
import io.vertx.mutiny.mqtt.messages.MqttPubRelMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Consumer;

/**
 * This handler is called when the server received a PUBREL package, and this communication only happened on QoS 2 and the direction is from client to server.<br/>
 * <br/>
 * Client  --PUBLISH QoS 2-->  SERVER<br/>
 * <br/>
 * Client  <-----PUBREC------  SERVER<br/>
 * <br/>
 * Client  ------PUBREL----->  SERVER<br/>
 * <br/>
 * Client  <-----PUBCOMP-----  SERVER<br/>
 * <br/>
 */
public class MqttPublishReleaseMessageHandler implements Consumer<MqttPubRelMessage> {

  private static final Logger LOGGER = LoggerFactory.getLogger(MqttPublishReleaseMessageHandler.class);

  private final MqttEndpoint mqttEndpoint;
  private final SessionService sessionService;
  private final MsgService msgService;
  private final CompositeService compositeService;

  public MqttPublishReleaseMessageHandler(MqttEndpoint mqttEndpoint, SessionService sessionService, MsgService msgService, CompositeService compositeService) {
    this.mqttEndpoint = mqttEndpoint;
    this.sessionService = sessionService;
    this.msgService = msgService;
    this.compositeService = compositeService;
  }

  @Override
  public void accept(MqttPubRelMessage mqttPubRelMessage) {
    String clientId = mqttEndpoint.clientIdentifier();
    if (LOGGER.isDebugEnabled()){
      LOGGER.debug("PUBREL from {}: {}", clientId, pubRelInfo(mqttPubRelMessage));
    }

    MqttProperties pubCompProperties = new MqttProperties();
    sessionService.getSession(clientId)
      .onItem().transformToUni(session -> msgService.removeInboundQos2Pub(session.getSessionId(), mqttPubRelMessage.messageId()))
      .onItem().transformToUni(inboundQos2Pub -> {
        if (mqttEndpoint.protocolVersion() <= MqttVersion.MQTT_3_1_1.protocolLevel()) {
          if (inboundQos2Pub == null) {
            LOGGER.warn("PUBREL from {} without having related PUBLISH packet", clientId);
          }
          mqttEndpoint.publishComplete(mqttPubRelMessage.messageId());
        } else {
          if (inboundQos2Pub == null) {
            LOGGER.warn("PUBREL from {} without having related PUBLISH packet", clientId);
            mqttEndpoint.publishComplete(mqttPubRelMessage.messageId(), MqttPubCompReasonCode.PACKET_IDENTIFIER_NOT_FOUND, pubCompProperties);
          } else {
            mqttEndpoint.publishComplete(mqttPubRelMessage.messageId(), MqttPubCompReasonCode.SUCCESS, pubCompProperties);
          }
        }
        return Uni.createFrom().item(inboundQos2Pub);
      })
      .onItem().transformToUni(inboundQos2Pub -> {
        if (inboundQos2Pub != null) {
          MsgToTopic msgToTopic = new MsgToTopic().setClientId(inboundQos2Pub.getClientId()).setTopic(inboundQos2Pub.getTopic()).setQos(inboundQos2Pub.getQos()).setPayload(inboundQos2Pub.getPayload()).setRetain(inboundQos2Pub.isRetain());
          return compositeService.forward(msgToTopic);
        } else {
          return Uni.createFrom().voidItem();
        }
      })
      .subscribe().with(ConsumerUtil.nothingToDo(), t -> LOGGER.error("Error occurred when handling MqttPubRelMessage", t));
  }

  private String pubRelInfo(MqttPubRelMessage mqttPubRelMessage) {
    JsonObject jsonObject = new JsonObject();
    jsonObject.put("messageId", mqttPubRelMessage.messageId());
    jsonObject.put("code", mqttPubRelMessage.code());
    jsonObject.put("properties", MqttPropertiesUtil.encode(mqttPubRelMessage.properties()));
    return jsonObject.toString();
  }

}
