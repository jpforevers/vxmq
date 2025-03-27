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

package io.github.jpforevers.vxmq.mqtt.handler;

import io.github.jpforevers.vxmq.assist.ConsumerUtil;
import io.github.jpforevers.vxmq.assist.MqttPropertiesUtil;
import io.github.jpforevers.vxmq.service.flow.FlowControlService;
import io.github.jpforevers.vxmq.service.msg.MsgService;
import io.github.jpforevers.vxmq.service.session.Session;
import io.github.jpforevers.vxmq.service.session.SessionService;
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
  private final FlowControlService flowControlService;

  public MqttPublishCompletionMessageHandler(MqttEndpoint mqttEndpoint, SessionService sessionService, MsgService msgService, FlowControlService flowControlService) {
    this.mqttEndpoint = mqttEndpoint;
    this.sessionService = sessionService;
    this.msgService = msgService;
    this.flowControlService = flowControlService;
  }

  @Override
  public void accept(MqttPubCompMessage mqttPubCompMessage) {
    if (LOGGER.isDebugEnabled()){
      LOGGER.debug("PUBCOMP from {}: {}", mqttEndpoint.clientIdentifier(), pubCompInfo(mqttPubCompMessage));
    }

    // From https://docs.oasis-open.org/mqtt/mqtt/v5.0/os/mqtt-v5.0-os.html#_Toc3901120: The Server MUST NOT send more than Receive Maximum QoS 1 and QoS 2 PUBLISH packets for which it has not received PUBACK, PUBCOMP, or PUBREC with a Reason Code of 128 or greater from the Client [MQTT-3.3.4-9]. If it receives more than Receive Maximum QoS 1 and QoS 2 PUBLISH packets where it has not sent a PUBACK or PUBCOMP in response, the Client uses DISCONNECT with Reason Code 0x93 (Receive Maximum exceeded) as described in section 4.13 Handling errors.
    // So, The MQTT broker should decrement the outbound reception number after received a PUBCOMP message.
    flowControlService.decrementOutboundReceive(mqttEndpoint.clientIdentifier());

    sessionService.getSessionByFields(mqttEndpoint.clientIdentifier(), new Session.Field[]{Session.Field.sessionId})
      .onItem().transformToUni(sessionFields -> msgService.getAndRemoveOutboundQos2Rel((String) sessionFields.get(Session.Field.sessionId), mqttPubCompMessage.messageId()))
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
