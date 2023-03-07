package cloud.wangyongjun.vxmq.mqtt.handler;

import cloud.wangyongjun.vxmq.assist.ConsumerUtil;
import cloud.wangyongjun.vxmq.mqtt.MqttPropertiesUtil;
import cloud.wangyongjun.vxmq.mqtt.msg.MsgService;
import cloud.wangyongjun.vxmq.mqtt.msg.OutboundQos2Rel;
import cloud.wangyongjun.vxmq.mqtt.session.SessionService;
import io.netty.handler.codec.mqtt.MqttProperties;
import io.netty.handler.codec.mqtt.MqttVersion;
import io.smallrye.mutiny.Uni;
import io.vertx.core.json.JsonObject;
import io.vertx.mqtt.messages.codes.MqttPubRelReasonCode;
import io.vertx.mutiny.mqtt.MqttEndpoint;
import io.vertx.mutiny.mqtt.messages.MqttPubRecMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.function.Consumer;

/**
 * This handler is called when the server received a PUBREC package, and this communication only happened with QoS 2 and the direction is from server to client.
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
public class MqttPublishReceivedMessageHandler implements Consumer<MqttPubRecMessage> {

  private static final Logger LOGGER = LoggerFactory.getLogger(MqttPublishReceivedMessageHandler.class);

  private final MqttEndpoint mqttEndpoint;
  private final SessionService sessionService;
  private final MsgService msgService;

  public MqttPublishReceivedMessageHandler(MqttEndpoint mqttEndpoint, SessionService sessionService, MsgService msgService) {
    this.mqttEndpoint = mqttEndpoint;
    this.sessionService = sessionService;
    this.msgService = msgService;
  }

  @Override
  public void accept(MqttPubRecMessage mqttPubRecMessage) {
    LOGGER.debug("PUBREC from {}: {}", mqttEndpoint.clientIdentifier(), pubRecInfo(mqttPubRecMessage));
    MqttProperties pubRelProperties = new MqttProperties();
    sessionService.getSession(mqttEndpoint.clientIdentifier())
      .onItem().transformToUni(session -> msgService.removeOutboundQos2Pub(session.getSessionId(), mqttPubRecMessage.messageId())
        .onItem().transformToUni(outboundQos2Pub -> {
          if (mqttEndpoint.protocolVersion() <= MqttVersion.MQTT_3_1_1.protocolLevel()) {
            if (outboundQos2Pub == null) {
              LOGGER.warn("PUBREC from {} without having related PUBLISH packet", mqttEndpoint.clientIdentifier());
            }
            mqttEndpoint.publishRelease(mqttPubRecMessage.messageId());
          } else {
            if (outboundQos2Pub == null) {
              LOGGER.warn("PUBREC from {} withozut having related PUBLISH packet", mqttEndpoint.clientIdentifier());
              mqttEndpoint.publishRelease(mqttPubRecMessage.messageId(), MqttPubRelReasonCode.PACKET_IDENTIFIER_NOT_FOUND, pubRelProperties);
            } else {
              mqttEndpoint.publishRelease(mqttPubRecMessage.messageId(), MqttPubRelReasonCode.SUCCESS, pubRelProperties);
            }
          }
          return Uni.createFrom().voidItem();
        })
        .onItem().transformToUni(v -> msgService.saveOutboundQos2Rel(new OutboundQos2Rel(session.getSessionId(), mqttEndpoint.clientIdentifier(), mqttPubRecMessage.messageId(), Instant.now().toEpochMilli()))))
      .subscribe().with(ConsumerUtil.nothingToDo(), t -> LOGGER.error("Error occurred when processing PUBREC from {}", mqttEndpoint.clientIdentifier(), t));
  }

  private String pubRecInfo(MqttPubRecMessage mqttPubRecMessage) {
    JsonObject jsonObject = new JsonObject();
    jsonObject.put("messageId", mqttPubRecMessage.messageId());
    jsonObject.put("code", mqttPubRecMessage.code());
    jsonObject.put("properties", MqttPropertiesUtil.encode(mqttPubRecMessage.properties()));
    return jsonObject.toString();
  }

}