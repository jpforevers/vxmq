package io.github.jpforevers.vxmq.service.alias;

import io.github.jpforevers.vxmq.assist.Config;
import io.github.jpforevers.vxmq.service.client.ClientService;
import io.github.jpforevers.vxmq.service.client.DisconnectRequest;
import io.github.jpforevers.vxmq.service.msg.MsgToTopic;
import io.github.jpforevers.vxmq.service.session.Session;
import io.github.jpforevers.vxmq.service.session.SessionService;
import io.netty.handler.codec.mqtt.MqttProperties;
import io.smallrye.mutiny.Uni;
import io.vertx.mqtt.messages.codes.MqttDisconnectReasonCode;
import io.vertx.mutiny.core.Vertx;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class InboundTopicAliasService {

  private static volatile InboundTopicAliasService inboundTopicAliasService;

  public static InboundTopicAliasService getSingleton(Vertx vertx, ClientService clientService, SessionService sessionService) {
    if (inboundTopicAliasService == null) {
      synchronized (InboundTopicAliasService.class) {
        if (inboundTopicAliasService == null) {
          inboundTopicAliasService = new InboundTopicAliasService(vertx, clientService, sessionService);
        }
      }
    }
    return inboundTopicAliasService;
  }

  private final Vertx vertx;
  private final ClientService clientService;
  private final SessionService sessionService;
  private final Map<String, Map<Integer, String>> clientIdToTopicAliasMap;

  private InboundTopicAliasService(Vertx vertx, ClientService clientService, SessionService sessionService) {
    this.vertx = vertx;
    this.clientService = clientService;
    this.sessionService = sessionService;
    this.clientIdToTopicAliasMap = new ConcurrentHashMap<>();
  }

  public Uni<Void> processTopicAlias(MsgToTopic msgToTopic, Integer topicAlias) {
    if (topicAlias != null) {
      // topic alias exist
      if (topicAlias > 0 && topicAlias <= Config.getMqttTopicAliasMax()) {
        // topic alias valid
        if (StringUtils.isNotBlank(msgToTopic.getTopic())) {
          // topic name exist
          saveTopicAlias(msgToTopic.getClientId(), topicAlias, msgToTopic.getTopic());
          return Uni.createFrom().voidItem();
        } else {
          // topic name not exist
          Optional<String> topicMapped = getTopicByAlias(msgToTopic.getClientId(), topicAlias);
          if (topicMapped.isPresent()) {
            // topic alias mapped exist
            msgToTopic.setTopic(topicMapped.get());
            return Uni.createFrom().voidItem();
          } else {
            // topic alias mapped not exist, close connection with 0x82
            return sessionService.getSessionByFields(msgToTopic.getClientId(), new Session.Field[]{Session.Field.verticleId})
              .onItem().transformToUni(sessionFields -> {
                DisconnectRequest disconnectRequest = new DisconnectRequest(MqttDisconnectReasonCode.PROTOCOL_ERROR, MqttProperties.NO_PROPERTIES);
                return clientService.disconnect((String) sessionFields.get(Session.Field.verticleId), disconnectRequest);
              });
          }
        }
      } else {
        // topic alias invalid, close connection with 0x94
        return sessionService.getSessionByFields(msgToTopic.getClientId(), new Session.Field[]{Session.Field.verticleId})
          .onItem().transformToUni(sessionFields -> {
            DisconnectRequest disconnectRequest = new DisconnectRequest(MqttDisconnectReasonCode.TOPIC_ALIAS_INVALID, MqttProperties.NO_PROPERTIES);
            return clientService.disconnect((String) sessionFields.get(Session.Field.verticleId), disconnectRequest);
          });
      }
    } else {
      // topic alias not exist
      return Uni.createFrom().voidItem();
    }
  }

  private void saveTopicAlias(String clientId, Integer topicAlias, String topic) {
    Map<Integer, String> topicAliasMap = clientIdToTopicAliasMap.computeIfAbsent(clientId, k -> new ConcurrentHashMap<>());
    topicAliasMap.put(topicAlias, topic);
  }

  public void clearTopicAlias(String clientId) {
    clientIdToTopicAliasMap.remove(clientId);
  }

  private Optional<String> getTopicByAlias(String clientId, Integer topicAlias) {
    Map<Integer, String> topicAliasMap = clientIdToTopicAliasMap.get(clientId);
    if (topicAliasMap != null) {
      return Optional.ofNullable(topicAliasMap.get(topicAlias));
    }
    return Optional.empty();
  }

}
