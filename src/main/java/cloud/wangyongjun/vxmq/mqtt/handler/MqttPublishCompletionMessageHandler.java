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
import cloud.wangyongjun.vxmq.service.msg.MsgService;
import cloud.wangyongjun.vxmq.service.session.SessionService;
import io.smallrye.mutiny.Uni;
import io.vertx.core.json.JsonObject;
import io.vertx.mutiny.mqtt.MqttEndpoint;
import io.vertx.mutiny.mqtt.messages.MqttPubCompMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Consumer;

/**
 * This handler is called when the server received a PUBCOMP package, and this communication only happened with QoS 2 and the direction is from server to client.
 * <br/>
 * SERVER  --PUBLISH QoS 2-->  Client<br/>
 * <br/>
 * SERVER  <-----PUBREC------  Client<br/>
 * <br/>
 * SERVER  ------PUBREL----->  Client<br/>
 * <br/>
 * SERVER  <-----PUBCOMP-----  Client<br/>
 * <br/>
 */
public class MqttPublishCompletionMessageHandler implements Consumer<MqttPubCompMessage> {

  private static final Logger LOGGER = LoggerFactory.getLogger(MqttPublishCompletionMessageHandler.class);

  private final MqttEndpoint mqttEndpoint;
  private final SessionService sessionService;
  private final MsgService msgService;

  public MqttPublishCompletionMessageHandler(MqttEndpoint mqttEndpoint, SessionService sessionService, MsgService msgService) {
    this.mqttEndpoint = mqttEndpoint;
    this.sessionService = sessionService;
    this.msgService = msgService;
  }

  @Override
  public void accept(MqttPubCompMessage mqttPubCompMessage) {
    if (LOGGER.isDebugEnabled()){
      LOGGER.debug("PUBCOMP from {}: {}", mqttEndpoint.clientIdentifier(), pubCompInfo(mqttPubCompMessage));
    }
    sessionService.getSession(mqttEndpoint.clientIdentifier())
      .onItem().transformToUni(session -> msgService.getAndRemoveOutboundQos2Rel(session.getSessionId(), mqttPubCompMessage.messageId()))
      .onItem().transformToUni(outboundQos2Rel -> {
        if (outboundQos2Rel == null) {
          LOGGER.warn("PUBCOMP from {} without having related PUBREL packet", mqttEndpoint.clientIdentifier());
        }
        return Uni.createFrom().voidItem();
      })
      .subscribe().with(ConsumerUtil.nothingToDo(), t -> LOGGER.error("Error occurred when processing PUBCOMP from {}", mqttEndpoint.clientIdentifier(), t));
  }

  private String pubCompInfo(MqttPubCompMessage mqttPubCompMessage) {
    JsonObject jsonObject = new JsonObject();
    jsonObject.put("messageId", mqttPubCompMessage.messageId());
    jsonObject.put("code", mqttPubCompMessage.code());
    jsonObject.put("properties", MqttPropertiesUtil.encode(mqttPubCompMessage.properties()));
    return jsonObject.toString();
  }

}
