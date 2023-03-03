package cloud.wangyongjun.vxmq.mqtt.handler;

import cloud.wangyongjun.vxmq.assist.ConsumerUtil;
import cloud.wangyongjun.vxmq.mqtt.MqttPropertiesUtil;
import cloud.wangyongjun.vxmq.mqtt.msg.MsgService;
import cloud.wangyongjun.vxmq.mqtt.session.SessionService;
import io.smallrye.mutiny.Uni;
import io.vertx.core.json.JsonObject;
import io.vertx.mutiny.mqtt.MqttEndpoint;
import io.vertx.mutiny.mqtt.messages.MqttPubAckMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Consumer;

/**
 * This handler will be called when the server received a PUBACK package, and this communication only happened with QoS 1 and the direction is from server to client.<br/>
 * <br/>
 * SERVER  --PUBLISH QoS 1-->  Client<br/>
 * <br/>
 * SERVER  <-----PUBACK------  Client<br/>
 * <br/>
 */
public class MqttPublishAcknowledgeMessageHandler implements Consumer<MqttPubAckMessage> {

  private final static Logger LOGGER = LoggerFactory.getLogger(MqttPublishAcknowledgeMessageHandler.class);

  private final MqttEndpoint mqttEndpoint;
  private final SessionService sessionService;
  private final MsgService msgService;

  public MqttPublishAcknowledgeMessageHandler(MqttEndpoint mqttEndpoint, SessionService sessionService, MsgService msgService) {
    this.mqttEndpoint = mqttEndpoint;
    this.sessionService = sessionService;
    this.msgService = msgService;
  }

  @Override
  public void accept(MqttPubAckMessage mqttPubAckMessage) {
    LOGGER.debug("PUBACK from {}: {}", mqttEndpoint.clientIdentifier(), pubAckInfo(mqttPubAckMessage));
    sessionService.getSession(mqttEndpoint.clientIdentifier())
      .onItem().transformToUni(session -> msgService.removeOutboundQos1Pub(session.getSessionId(), mqttPubAckMessage.messageId()))
      .onItem().transformToUni(ifExist -> {
        if (!ifExist) {
          LOGGER.warn("PUBACK from {} with messageId {} without having related PUBLISH packet", mqttEndpoint.clientIdentifier(), mqttPubAckMessage.messageId());
        }
        return Uni.createFrom().voidItem();
      })
      .subscribe().with(ConsumerUtil.nothingToDo(), t -> LOGGER.error("Error occurred when processing PUBACK from {}", mqttEndpoint.clientIdentifier(), t));
  }

  private String pubAckInfo(MqttPubAckMessage mqttPubAckMessage) {
    JsonObject jsonObject = new JsonObject();
    jsonObject.put("messageId", mqttPubAckMessage.messageId());
    jsonObject.put("code", mqttPubAckMessage.code());
    jsonObject.put("properties", MqttPropertiesUtil.encode(mqttPubAckMessage.properties()));
    return jsonObject.toString();
  }

}
