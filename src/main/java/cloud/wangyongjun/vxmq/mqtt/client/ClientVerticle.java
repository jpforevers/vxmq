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

package cloud.wangyongjun.vxmq.mqtt.client;

import cloud.wangyongjun.vxmq.assist.ConsumerUtil;
import cloud.wangyongjun.vxmq.assist.EBHeader;
import cloud.wangyongjun.vxmq.mqtt.msg.MsgService;
import cloud.wangyongjun.vxmq.mqtt.msg.MsgToClient;
import cloud.wangyongjun.vxmq.mqtt.msg.OutboundQos1Pub;
import cloud.wangyongjun.vxmq.mqtt.msg.OutboundQos2Pub;
import cloud.wangyongjun.vxmq.mqtt.session.SessionService;
import io.netty.handler.codec.mqtt.MqttQoS;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.vertx.core.AbstractVerticle;
import io.vertx.core.json.JsonObject;
import io.vertx.mutiny.core.buffer.Buffer;
import io.vertx.mutiny.core.eventbus.Message;
import io.vertx.mutiny.mqtt.MqttEndpoint;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;

public class ClientVerticle extends AbstractVerticle {

  private final static Logger LOGGER = LoggerFactory.getLogger(ClientVerticle.class);

  private static final int MAX_MESSAGE_ID = 65535;

  private final MqttEndpoint mqttEndpoint;
  private final SessionService sessionService;
  private final MsgService msgService;
  private int messageIdCounter;

  public ClientVerticle(MqttEndpoint mqttEndpoint, SessionService sessionService, MsgService msgService) {
    this.mqttEndpoint = mqttEndpoint;
    this.sessionService = sessionService;
    this.msgService = msgService;
  }

  @Override
  public Uni<Void> asyncStart() {
    return vertx.eventBus().consumer(deploymentID(), this::ebMessageHandler).completionHandler();
  }

  @Override
  public Uni<Void> asyncStop() {
    return Uni.createFrom().voidItem();
  }

  private void ebMessageHandler(Message<JsonObject> actionMessage) {
    String actionString = actionMessage.headers().get(EBHeader.ACTION.name());
    if (StringUtils.isNotBlank(actionString)) {
      ClientVerticleAction actionEnum = ClientVerticleAction.valueOf(actionString);
      switch (actionEnum) {
        case UNDEPLOY_CLIENT_VERTICLE -> handleUndeployClientVerticleAction();
        case CLOSE_MQTT_ENDPOINT -> handleCloseMqttEndpointAction();
        case DISCONNECT -> handleDisconnectAction(actionMessage);
        case SEND_PUBLISH -> handleSendPublish(actionMessage);
      }
    }
  }

  private void handleUndeployClientVerticleAction() {
    vertx.undeploy(deploymentID()).subscribe().with(ConsumerUtil.nothingToDo(), t -> LOGGER.error("Error occurred when handle undeploy client verticle action", t));
  }

  private void handleCloseMqttEndpointAction() {
    mqttEndpoint.close();
  }

  private void handleDisconnectAction(Message<JsonObject> actionMessage) {
    DisconnectRequest disconnectRequest = new DisconnectRequest(actionMessage.body());
    mqttEndpoint.disconnect(disconnectRequest.getMqttDisconnectReasonCode(), disconnectRequest.getDisconnectProperties());
  }

  private void handleSendPublish(Message<JsonObject> actionMessage) {
    MsgToClient msgToClient = new MsgToClient(actionMessage.body());
    int messageId;
    if (msgToClient.getMessageId() == null || msgToClient.getMessageId() <= 0 || msgToClient.getMessageId() >= MAX_MESSAGE_ID) {
      this.messageIdCounter = ((messageIdCounter % MAX_MESSAGE_ID) != 0) ? messageIdCounter + 1 : 1;
      messageId = this.messageIdCounter;
    } else {
      messageId = msgToClient.getMessageId();
    }
    sessionService.getSession(mqttEndpoint.clientIdentifier())
      .onItem().transformToUni(session -> {
        MqttQoS mqttQoS = MqttQoS.valueOf(msgToClient.getQos());
        return switch (mqttQoS) {
          case AT_LEAST_ONCE ->
            msgService.saveOutboundQos1Pub(new OutboundQos1Pub(session.getSessionId(), session.getClientId(), messageId, msgToClient.getTopic(), msgToClient.getQos(), msgToClient.getPayload(), msgToClient.isDup(), msgToClient.isRetain(), Instant.now().toEpochMilli()));
          case EXACTLY_ONCE ->
            msgService.saveOutboundQos2Pub(new OutboundQos2Pub(session.getSessionId(), session.getClientId(), messageId, msgToClient.getTopic(), msgToClient.getQos(), msgToClient.getPayload(), msgToClient.isDup(), msgToClient.isRetain(), Instant.now().toEpochMilli()));
          default -> Uni.createFrom().voidItem();
        };
      })
      .onItem().transformToUni(v -> mqttEndpoint.publish(msgToClient.getTopic(), Buffer.newInstance(msgToClient.getPayload()), MqttQoS.valueOf(msgToClient.getQos()), msgToClient.isDup(), msgToClient.isRetain(), messageId))
      .subscribe().with(ConsumerUtil.nothingToDo(), t -> LOGGER.error("Error occurred when sending PUBLISH to client " + mqttEndpoint.clientIdentifier(), t));
  }

  public String getClientId() {
    return mqttEndpoint.clientIdentifier();
  }

}
