package io.github.jpforevers.vxmq.service.alias;

import io.github.jpforevers.vxmq.service.msg.MsgToClient;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.core.Vertx;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class OutboundTopicAliasService {

  private static volatile OutboundTopicAliasService outboundTopicAliasService;

  public static OutboundTopicAliasService getSingleton(Vertx vertx) {
    if (outboundTopicAliasService == null) {
      synchronized (OutboundTopicAliasService.class) {
        if (outboundTopicAliasService == null) {
          outboundTopicAliasService = new OutboundTopicAliasService(vertx);
        }
      }
    }
    return outboundTopicAliasService;
  }

  private final Vertx vertx;
  private final Map<String, Map<String, Integer>> clientIdToTopicAliasMap;
  private final Map<String, Integer> clientIdToCurrentAliasMap;

  private OutboundTopicAliasService(Vertx vertx) {
    this.vertx = vertx;
    this.clientIdToTopicAliasMap = new ConcurrentHashMap<>();
    this.clientIdToCurrentAliasMap = new ConcurrentHashMap<>();
  }

  public Uni<Void> processTopicAlias(MsgToClient msgToClient, String clientId, Integer topicAliasMax) {
    if (topicAliasMax != null && topicAliasMax > 0) {
      // topic alias max exist and gt 0
      Optional<Integer> topicAliasOptional = getAliasByTopic(clientId, msgToClient.getTopic());
      if (topicAliasOptional.isPresent()) {
        // topic alias exist
        msgToClient.setTopicAlias(topicAliasOptional.get());
        msgToClient.setTopic("");
      } else {
        // topic alias not exist
        clientIdToTopicAliasMap.compute(clientId, (key, value) -> {
          if (value == null) {
            value = new ConcurrentHashMap<>();
          }
          value.computeIfAbsent(msgToClient.getTopic(), k -> {
            Integer next = clientIdToCurrentAliasMap.getOrDefault(clientId, 0) + 1;
            if (next <= topicAliasMax) {
              msgToClient.setTopicAlias(next);
              clientIdToCurrentAliasMap.put(clientId, next);
              return next;
            } else {
              return null;
            }
          });
          return value;
        });
      }
    }
    return Uni.createFrom().voidItem();
  }

  public void clearTopicAlias(String clientId) {
    clientIdToTopicAliasMap.remove(clientId);
    clientIdToCurrentAliasMap.remove(clientId);
  }

  private Optional<Integer> getAliasByTopic(String clientId, String topic) {
    Map<String, Integer> topicAliasMap = clientIdToTopicAliasMap.get(clientId);
    if (topicAliasMap != null) {
      return Optional.ofNullable(topicAliasMap.get(topic));
    }
    return Optional.empty();
  }

}
