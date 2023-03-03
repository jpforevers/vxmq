package cloud.wangyongjun.vxmq.mqtt.handler;

import cloud.wangyongjun.vxmq.assist.ConsumerUtil;
import cloud.wangyongjun.vxmq.mqtt.MqttPropertiesUtil;
import cloud.wangyongjun.vxmq.mqtt.msg.MsgService;
import cloud.wangyongjun.vxmq.mqtt.session.SessionService;
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
    LOGGER.debug("PUBCOMP from {}: {}", mqttEndpoint.clientIdentifier(), pubCompInfo(mqttPubCompMessage));
    sessionService.getSession(mqttEndpoint.clientIdentifier())
      .onItem().transformToUni(session -> msgService.removeOutboundQos2Rel(session.getSessionId(), mqttPubCompMessage.messageId()))
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
