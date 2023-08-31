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

package cloud.wangyongjun.vxmq.service.composite;

import cloud.wangyongjun.vxmq.assist.Config;
import cloud.wangyongjun.vxmq.assist.IgniteAssist;
import cloud.wangyongjun.vxmq.assist.IgniteUtil;
import cloud.wangyongjun.vxmq.http.api.ApiErrorCode;
import cloud.wangyongjun.vxmq.http.api.ApiException;
import cloud.wangyongjun.vxmq.service.client.ClientService;
import cloud.wangyongjun.vxmq.service.msg.MsgService;
import cloud.wangyongjun.vxmq.service.msg.MsgToClient;
import cloud.wangyongjun.vxmq.service.msg.MsgToTopic;
import cloud.wangyongjun.vxmq.service.retain.Retain;
import cloud.wangyongjun.vxmq.service.retain.RetainService;
import cloud.wangyongjun.vxmq.service.session.Session;
import cloud.wangyongjun.vxmq.service.session.SessionService;
import cloud.wangyongjun.vxmq.service.sub.Subscription;
import cloud.wangyongjun.vxmq.service.sub.mutiny.SubService;
import cloud.wangyongjun.vxmq.service.will.WillService;
import io.netty.handler.codec.mqtt.MqttVersion;
import io.smallrye.mutiny.Uni;
import io.vertx.core.json.JsonObject;
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

  public static DefaultCompositeService getSingleton(Vertx vertx, JsonObject config, SessionService sessionService, SubService subService, WillService willService,
                                                     MsgService msgService, RetainService retainService, ClientService clientService) {
    if (defaultCompositeService == null) {
      synchronized (DefaultCompositeService.class) {
        if (defaultCompositeService == null) {
          defaultCompositeService = new DefaultCompositeService(vertx, config, sessionService, subService, willService, msgService, retainService, clientService);
        }
      }
    }
    return defaultCompositeService;
  }

  private final Vertx vertx;
  private final JsonObject config;
  private final SessionService sessionService;
  private final SubService subService;
  private final WillService willService;
  private final MsgService msgService;
  private final RetainService retainService;
  private final ClientService clientService;

  private DefaultCompositeService(Vertx vertx, JsonObject config, SessionService sessionService, SubService subService, WillService willService,
                                  MsgService msgService, RetainService retainService, ClientService clientService) {
    this.vertx = vertx;
    this.config = config;
    this.sessionService = sessionService;
    this.subService = subService;
    this.willService = willService;
    this.msgService = msgService;
    this.retainService = retainService;
    this.clientService = clientService;
  }

  @Override
  public Uni<Void> clearSessionData(String clientId) {
    return Uni.createFrom().voidItem()
      .onItem().transformToUni(v -> sessionService.getSession(clientId))
      .onItem().ifNotNull().transformToUni(session -> Uni.createFrom().voidItem()
        .onItem().transformToUni(v -> msgService.clearMsgs(session.getSessionId()))
        .onItem().transformToUni(v -> subService.clearSubs(session.getSessionId()))
        .onItem().transformToUni(v -> sessionService.removeSession(clientId)));
  }

  @Override
  public Uni<Void> publishWill(String sessionId) {
    return Uni.createFrom().voidItem()
      .onItem().transformToUni(v -> willService.getWill(sessionId))
      .onItem().transformToUni(will -> {
        if (will != null) {
          MsgToTopic msgToTopic = new MsgToTopic().setClientId(will.getClientId()).setTopic(will.getWillTopicName())
            .setQos(will.getWillQos()).setPayload(will.getWillMessage()).setRetain(will.isWillRetain());
          return forward(msgToTopic)
            .onItem().transformToUni(v -> willService.removeWill(sessionId))
            .onItem().transformToUni(v -> {
              if (will.isWillRetain()) {
                if (will.getWillMessage() != null && will.getWillMessage().length() > 0) {
                  Retain retain = new Retain(will.getWillTopicName(), will.getWillQos(), will.getWillMessage(), Instant.now().toEpochMilli());
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
            return msgService.saveOfflineMsg(msgToClient);
          } else {
            return Uni.createFrom().voidItem();
          }
        } else {
          return msgService.saveOfflineMsg(msgToClient);
        }
      }
    } else {
      return Uni.createFrom().voidItem();
    }
  }

  @Override
  public Uni<Void> forward(MsgToTopic msgToTopic) {
//    return Uni.createFrom().emitter(uniEmitter -> {
//      subService.allMatchSubs(msgToTopic.getTopic(), true)
//        .onItem().transformToMulti(subscriptions -> Multi.createFrom().items(subscriptions.stream()))
//        .onItem().call(subscription -> {
//          if (subscription.getNoLocal() != null && subscription.getNoLocal() && subscription.getClientId().equals(msgToTopic.getClientId())) {
//            return Uni.createFrom().voidItem();
//          }else {
//            return sessionService.getSession(subscription.getClientId())
//              .onItem().transformToUni(session -> {
//                MsgToClient msgToClient = new MsgToClient().setSessionId(session.getSessionId()).setClientId(subscription.getClientId())
//                  .setTopic(msgToTopic.getTopic()).setQos(Math.min(msgToTopic.getQos(), subscription.getQos()))
//                  .setPayload(msgToTopic.getPayload()).setDup(false).setCreatedTime(Instant.now().toEpochMilli());
//                if (session.getProtocolLevel() <= MqttVersion.MQTT_3_1_1.protocolLevel()) {
//                  // From http://docs.oasis-open.org/mqtt/mqtt/v3.1.1/os/mqtt-v3.1.1-os.html#_Toc398718038
//                  // When sending a PUBLISH Packet to a Client the Server MUST set the RETAIN flag to 1 if a message is sent as a result of a new subscription being made by a Client [MQTT-3.3.1-8]. It MUST set the RETAIN flag to 0 when a PUBLISH Packet is sent to a Client because it matches an established subscription regardless of how the flag was set in the message it received [MQTT-3.3.1-9].
//                  msgToClient.setRetain(false);
//                } else {
//                  // From https://docs.oasis-open.org/mqtt/mqtt/v5.0/os/mqtt-v5.0-os.html#_Subscription_Options
//                  // Bit 3 of the Subscription Options represents the Retain As Published option. If 1, Application Messages forwarded using this subscription keep the RETAIN flag they were published with. If 0, Application Messages forwarded using this subscription have the RETAIN flag set to 0. Retained messages sent when the subscription is established have the RETAIN flag set to 1.
//                  msgToClient.setRetain(subscription.getRetainAsPublished() && msgToTopic.isRetain());
//                }
//                return sendToClient(session, msgToClient);
//              });
//          }
//        }).subscribe().with(ConsumerUtil.nothingToDo(), uniEmitter::fail, () -> uniEmitter.complete(null));
//    });


    return Uni.createFrom().voidItem()
      .onItem().transformToUni(v -> subService.allMatchSubs(msgToTopic.getTopic(), true))
      .onItem().transformToUni(subscriptions -> {
        List<Uni<Void>> unis = new ArrayList<>();
        for (Subscription subscription : subscriptions) {
          if (subscription.getNoLocal() != null && subscription.getNoLocal() && subscription.getClientId().equals(msgToTopic.getClientId())) {
            // Bit 2 of the Subscription Options represents the No Local option. If the value is 1, Application Messages MUST NOT be forwarded to a connection with a ClientID equal to the ClientID of the publishing connection [MQTT-3.8.3-3].
            break;
          } else {
            Uni<Void> unix = sessionService.getSession(subscription.getClientId())
              .onItem().transformToUni(session -> {
                MsgToClient msgToClient = new MsgToClient().setSessionId(session.getSessionId()).setClientId(subscription.getClientId())
                  .setTopic(msgToTopic.getTopic()).setQos(Math.min(msgToTopic.getQos(), subscription.getQos()))
                  .setPayload(msgToTopic.getPayload()).setDup(false).setCreatedTime(Instant.now().toEpochMilli());
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
    MsgToClient msgToClient = IgniteAssist.getOfflineMsgQueueOfSession(IgniteUtil.getIgnite(vertx), sessionId, config).poll();
    if (msgToClient != null) {
      return Uni.createFrom().voidItem()
        .onItem().transformToUni(v -> sessionService.getSession(msgToClient.getClientId()))
        .onItem().transformToUni(session -> sendToClient(session, msgToClient))
        .onItem().transformToUni(v -> sendOfflineMsg(sessionId));
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
            return clientService.closeMqttEndpoint(session.getVerticleId());
          } else {
            return Uni.createFrom().voidItem()
              .onItem().transformToUni(vv -> obtainClientLock(clientId))
              .onItem().transformToUni(vv -> this.clearSessionData(clientId))
              .onItemOrFailure().call((vv, t) -> releaseClientLock(clientId));
          }
        }
      });
  }

  /**
   * Get client lock
   *
   * @param clientId clientId
   * @return Void
   */
  private Uni<Void> obtainClientLock(String clientId) {
    return clientService.obtainClientLock(clientId, 5000)
      .onItem().invoke(lock -> {
        if (LOGGER.isDebugEnabled()) {
          LOGGER.debug("Client lock obtained for {}", clientId);
        }
      });
  }

  /**
   * Release client lock
   *
   * @param clientId clientId
   * @return Void
   */
  private Uni<Void> releaseClientLock(String clientId) {
    return clientService.releaseClientLock(clientId)
      .onItem().invoke(v -> {
        if (LOGGER.isDebugEnabled()) {
          LOGGER.debug("Client lock released for {}", clientId);
        }
      });
  }

}
