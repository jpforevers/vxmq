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

package io.github.jpforevers.vxmq.service.composite;

import io.github.jpforevers.vxmq.assist.Config;
import io.github.jpforevers.vxmq.assist.IgniteAssist;
import io.github.jpforevers.vxmq.assist.IgniteUtil;
import io.github.jpforevers.vxmq.http.api.ApiErrorCode;
import io.github.jpforevers.vxmq.http.api.ApiException;
import io.github.jpforevers.vxmq.service.alias.InboundTopicAliasService;
import io.github.jpforevers.vxmq.service.client.ClientService;
import io.github.jpforevers.vxmq.service.client.CloseMqttEndpointRequest;
import io.github.jpforevers.vxmq.service.msg.MsgService;
import io.github.jpforevers.vxmq.service.msg.MsgToClient;
import io.github.jpforevers.vxmq.service.msg.MsgToTopic;
import io.github.jpforevers.vxmq.service.retain.Retain;
import io.github.jpforevers.vxmq.service.retain.RetainService;
import io.github.jpforevers.vxmq.service.session.Session;
import io.github.jpforevers.vxmq.service.session.SessionService;
import io.github.jpforevers.vxmq.service.sub.Subscription;
import io.github.jpforevers.vxmq.service.sub.mutiny.SubService;
import io.github.jpforevers.vxmq.service.will.WillService;
import io.netty.handler.codec.mqtt.MqttVersion;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.core.Vertx;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class DefaultCompositeService implements CompositeService {

  private final static Logger LOGGER = LoggerFactory.getLogger(DefaultCompositeService.class);

  private static volatile DefaultCompositeService defaultCompositeService;

  public static DefaultCompositeService getSingleton(Vertx vertx, SessionService sessionService, SubService subService,
                                                     WillService willService, MsgService msgService, RetainService retainService,
                                                     ClientService clientService, InboundTopicAliasService inboundTopicAliasService) {
    if (defaultCompositeService == null) {
      synchronized (DefaultCompositeService.class) {
        if (defaultCompositeService == null) {
          defaultCompositeService = new DefaultCompositeService(vertx, sessionService, subService, willService,
            msgService, retainService, clientService, inboundTopicAliasService);
        }
      }
    }
    return defaultCompositeService;
  }

  private final Vertx vertx;
  private final SessionService sessionService;
  private final SubService subService;
  private final WillService willService;
  private final MsgService msgService;
  private final RetainService retainService;
  private final ClientService clientService;
  private final InboundTopicAliasService inboundTopicAliasService;

  private DefaultCompositeService(Vertx vertx, SessionService sessionService, SubService subService,
                                  WillService willService, MsgService msgService, RetainService retainService,
                                  ClientService clientService, InboundTopicAliasService inboundTopicAliasService) {
    this.vertx = vertx;
    this.sessionService = sessionService;
    this.subService = subService;
    this.willService = willService;
    this.msgService = msgService;
    this.retainService = retainService;
    this.clientService = clientService;
    this.inboundTopicAliasService = inboundTopicAliasService;
  }

  @Override
  public Uni<Void> clearSessionData(String clientId) {
    return Uni.createFrom().voidItem()
      .onItem().transformToUni(v -> sessionService.getSessionByFields(clientId, new Session.Field[]{Session.Field.sessionId}))
      .onItem().ifNotNull().transformToUni(sessionFields -> Uni.createFrom().voidItem()
        .onItem().transformToUni(v -> msgService.clearMsgs((String) sessionFields.get(Session.Field.sessionId)))
        .onItem().transformToUni(v -> subService.clearSubs((String) sessionFields.get(Session.Field.sessionId)))
        .onItem().transformToUni(v -> sessionService.removeSession(clientId)));
  }

  @Override
  public Uni<Void> publishWill(String sessionId) {
    return Uni.createFrom().voidItem()
      .onItem().transformToUni(v -> willService.getWill(sessionId))
      .onItem().transformToUni(will -> {
        if (will != null) {
          MsgToTopic msgToTopic = new MsgToTopic().setClientId(will.getClientId()).setTopic(will.getWillTopicName())
            .setQos(will.getWillQos()).setPayload(will.getWillMessage()).setRetain(will.isWillRetain())
            .setMessageExpiryInterval(will.getMessageExpiryInterval())
            .setPayloadFormatIndicator(will.getPayloadFormatIndicator())
            .setContentType(will.getContentType())
            .setResponseTopic(will.getResponseTopic())
            .setCorrelationData(will.getCorrelationData())
            .setUserProperties(will.getUserProperties());
          return forward(msgToTopic)
            .onItem().transformToUni(v -> willService.removeWill(sessionId))
            .onItem().transformToUni(v -> {
              if (will.isWillRetain()) {
                if (will.getWillMessage() != null && will.getWillMessage().length() > 0) {
                  Retain retain = new Retain(will.getWillTopicName(), will.getWillQos(), will.getWillMessage(), will.getMessageExpiryInterval(), will.getPayloadFormatIndicator(), will.getContentType(), Instant.now().toEpochMilli());
                  return retainService.saveOrUpdateRetain(retain);
                } else {
                  return retainService.removeRetain(will.getWillTopicName());
                }
              } else {
                return Uni.createFrom().voidItem();
              }
            });
        } else {
          return Uni.createFrom().voidItem();
        }
      });
  }

  @Override
  public Uni<Void> sendToClient(Session session, MsgToClient msgToClient) {
    if (session != null) {
      if (session.isOnline()) {
        return clientService.sendPublish(session.getVerticleId(), msgToClient);
      } else {
        if (session.getProtocolLevel() <= MqttVersion.MQTT_3_1_1.protocolLevel()) {
          if (!session.isCleanSession()) {
            // For MQTT 3.1.1, should saving offline message when the cleanSession is false.
            return msgService.saveOfflineMsg(msgToClient);
          } else {
            return Uni.createFrom().voidItem();
          }
        } else {
          // For MQTT 5, because of "Session Expiry Interval", should always saving offline message here, and clean it when "Session Expiry Interval" end.
          return msgService.saveOfflineMsg(msgToClient);
        }
      }
    } else {
      return Uni.createFrom().voidItem();
    }
  }

  @Override
  public Uni<Void> forward(MsgToTopic msgToTopic) {
    return Uni.createFrom().voidItem()
      .onItem().transformToUni(v -> inboundTopicAliasService.processTopicAlias(msgToTopic, msgToTopic.getTopicAlias()))
      .onItem().transformToUni(v -> {
        if (StringUtils.isNotBlank(msgToTopic.getTopic())) {
          return subService.allMatchSubs(msgToTopic.getTopic(), false);
        } else {
          return Uni.createFrom().item(List.<Subscription>of());
        }
      })
      .onItem().transformToUni(subscriptions -> {
        List<Uni<Void>> unis = new ArrayList<>();
        for (Subscription subscription : subscriptions) {
          if (subscription.getNoLocal() != null && subscription.getNoLocal() && subscription.getClientId().equals(msgToTopic.getClientId())) {
            // Bit 2 of the Subscription Options represents the No Local option. If the value is 1, Application Messages MUST NOT be forwarded to a connection with a ClientID equal to the ClientID of the publishing connection [MQTT-3.8.3-3].
            break;
          } else {
            Uni<Void> unix = sessionService.getSession(subscription.getClientId())
              .onItem().transformToUni(session -> {
                MsgToClient msgToClient = new MsgToClient()
                  .setSessionId(session.getSessionId()).setClientId(subscription.getClientId()).setTopic(msgToTopic.getTopic())
                  .setQos(Math.min(msgToTopic.getQos(), subscription.getQos())).setPayload(msgToTopic.getPayload()).setDup(false)
                  .setMessageExpiryInterval(msgToTopic.getMessageExpiryInterval()).setPayloadFormatIndicator(msgToTopic.getPayloadFormatIndicator())
                  .setContentType(msgToTopic.getContentType()).setResponseTopic(msgToTopic.getResponseTopic())
                  .setCorrelationData(msgToTopic.getCorrelationData()).setSubscriptionIdentifier(subscription.getSubscriptionIdentifier())
                  .setUserProperties(msgToTopic.getUserProperties()).setCreatedTime(Instant.now().toEpochMilli());
                if (session.getProtocolLevel() <= MqttVersion.MQTT_3_1_1.protocolLevel()) {
                  // From http://docs.oasis-open.org/mqtt/mqtt/v3.1.1/os/mqtt-v3.1.1-os.html#_Toc398718038
                  // When sending a PUBLISH Packet to a Client the Server MUST set the RETAIN flag to 1 if a message is sent as a result of a new subscription being made by a Client [MQTT-3.3.1-8]. It MUST set the RETAIN flag to 0 when a PUBLISH Packet is sent to a Client because it matches an established subscription regardless of how the flag was set in the message it received [MQTT-3.3.1-9].
                  msgToClient.setRetain(false);
                } else {
                  // From https://docs.oasis-open.org/mqtt/mqtt/v5.0/os/mqtt-v5.0-os.html#_Subscription_Options
                  // Bit 3 of the Subscription Options represents the Retain As Published option. If 1, Application Messages forwarded using this subscription keep the RETAIN flag they were published with. If 0, Application Messages forwarded using this subscription have the RETAIN flag set to 0. Retained messages sent when the subscription is established have the RETAIN flag set to 1.
                  msgToClient.setRetain(subscription.getRetainAsPublished() && msgToTopic.isRetain());
                }
                return sendToClient(session, msgToClient);
              });
            unis.add(unix);
          }
        }
        return !unis.isEmpty() ? Uni.combine().all().unis(unis).usingConcurrencyOf(Config.AVAILABLE_CPU_CORE_SENSORS * 2).collectFailures().discardItems() : Uni.createFrom().voidItem();
      });
  }

  // Send offline messages sequentially through recursion!
  @Override
  public Uni<Void> sendOfflineMsg(String sessionId) {
    MsgToClient msgToClient = IgniteAssist.getOfflineMsgQueueOfSession(IgniteUtil.getIgnite(vertx), sessionId).poll();
    if (msgToClient != null) {
      Instant now = Instant.now();
      if (msgToClient.getMessageExpiryInterval() != null && msgToClient.getMessageExpiryInterval() != 0
        && msgToClient.getCreatedTime() + msgToClient.getMessageExpiryInterval() * 1000 < now.toEpochMilli()) {
        // Message expiry interval exist and already expired, skip this message.
        LOGGER.warn("Offline message expired: {}", msgToClient);
        return sendOfflineMsg(sessionId);
      } else {
        return Uni.createFrom().voidItem()
          .onItem().transformToUni(v -> sessionService.getSession(msgToClient.getClientId()))
          .onItem().transformToUni(session -> sendToClient(session, msgToClient))
          .onItem().transformToUni(v -> sendOfflineMsg(sessionId));
      }
    } else {
      return Uni.createFrom().voidItem();
    }
  }

  @Override
  public Uni<Void> deleteSession(String clientId) {
    return Uni.createFrom().voidItem()
      .onItem().transformToUni(v -> sessionService.getSession(clientId))
      .onItem().transformToUni(session -> {
        if (session == null) {
          return Uni.createFrom().failure(new ApiException(ApiErrorCode.COMMON_NOT_FOUND, "Client session not found: " + clientId));
        } else {
          if (session.isOnline() && StringUtils.isNotBlank(session.getVerticleId())) {
            return clientService.closeMqttEndpoint(session.getVerticleId(), new CloseMqttEndpointRequest());
          } else {
            return Uni.createFrom().voidItem()
              .onItem().transformToUni(vv -> clientService.obtainClientLock(clientId, 5000))
              .onItem().transformToUni(vv -> this.clearSessionData(clientId))
              .eventually(() -> clientService.releaseClientLock(clientId));
          }
        }
      });
  }

}
