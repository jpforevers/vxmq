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

import cloud.wangyongjun.vxmq.assist.*;
import cloud.wangyongjun.vxmq.event.*;
import cloud.wangyongjun.vxmq.event.mqtt.MqttConnectFailedEvent;
import cloud.wangyongjun.vxmq.event.mqtt.MqttConnectedEvent;
import cloud.wangyongjun.vxmq.event.mqtt.MqttEndpointClosedEvent;
import cloud.wangyongjun.vxmq.event.mqtt.MqttSessionTakenOverEvent;
import cloud.wangyongjun.vxmq.mqtt.exception.MqttAuthFailedException;
import cloud.wangyongjun.vxmq.service.authentication.MqttAuthData;
import cloud.wangyongjun.vxmq.service.authentication.mutiny.AuthenticationService;
import cloud.wangyongjun.vxmq.service.client.ClientService;
import cloud.wangyongjun.vxmq.service.client.ClientVerticle;
import cloud.wangyongjun.vxmq.service.client.DisconnectRequest;
import cloud.wangyongjun.vxmq.service.composite.CompositeService;
import cloud.wangyongjun.vxmq.service.msg.MsgService;
import cloud.wangyongjun.vxmq.service.retain.RetainService;
import cloud.wangyongjun.vxmq.service.session.Session;
import cloud.wangyongjun.vxmq.service.session.SessionService;
import cloud.wangyongjun.vxmq.service.sub.mutiny.SubService;
import cloud.wangyongjun.vxmq.service.will.Will;
import cloud.wangyongjun.vxmq.service.will.WillService;
import io.netty.handler.codec.mqtt.MqttConnectReturnCode;
import io.netty.handler.codec.mqtt.MqttProperties;
import io.netty.handler.codec.mqtt.MqttQoS;
import io.netty.handler.codec.mqtt.MqttVersion;
import io.smallrye.mutiny.Context;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;
import io.vertx.mqtt.MqttAuth;
import io.vertx.mqtt.messages.codes.MqttDisconnectReasonCode;
import io.vertx.mqtt.messages.codes.MqttPubRelReasonCode;
import io.vertx.mutiny.core.Vertx;
import io.vertx.mutiny.core.eventbus.MessageConsumer;
import io.vertx.mutiny.mqtt.MqttEndpoint;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * If an MQTT client connect to the server a new MqttEndpoint instance will be created and passed to the handler.
 */
public class MqttEndpointHandler implements Consumer<MqttEndpoint> {

  private final static String CONTEXT_KEY_SESSION_PRESENT = "sessionPresent";
  private final static String CONTEXT_KEY_SESSION_TAKEN_OVER = "sessionTakenOver";
  private final static String CONTEXT_KEY_SESSION_TAKEN_OVER_OLD_SESSION = "sessionTakenOverOldSession";

  private final static Logger LOGGER = LoggerFactory.getLogger(MqttEndpointHandler.class);

  private final Vertx vertx;
  private final SessionService sessionService;
  private final MsgService msgService;
  private final WillService willService;
  private final ClientService clientService;
  private final SubService subService;
  private final RetainService retainService;
  private final CompositeService compositeService;
  private final EventService eventService;
  private final AuthenticationService authenticationService;

  public MqttEndpointHandler(Vertx vertx,
                             SessionService sessionService,
                             MsgService msgService,
                             WillService willService,
                             ClientService clientService,
                             SubService subService,
                             RetainService retainService,
                             CompositeService compositeService,
                             EventService eventService,
                             AuthenticationService authenticationService) {
    this.vertx = vertx;
    this.sessionService = sessionService;
    this.msgService = msgService;
    this.willService = willService;
    this.clientService = clientService;
    this.subService = subService;
    this.retainService = retainService;
    this.compositeService = compositeService;
    this.eventService = eventService;
    this.authenticationService = authenticationService;
  }

  @Override
  public void accept(MqttEndpoint mqttEndpoint) {
    String clientIdOriginal = mqttEndpoint.clientIdentifier();
    if (StringUtils.isBlank(clientIdOriginal)) {
      mqttEndpoint.setClientIdentifier(UUIDUtil.timeBasedUuid().toString());
    }
    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("CONNECT from {}: {}", mqttEndpoint.clientIdentifier(), connectInfo(mqttEndpoint));
    }

    MqttProperties connAckProperties = new MqttProperties();
    if (mqttEndpoint.protocolVersion() > MqttVersion.MQTT_3_1_1.protocolLevel()) {
      if (StringUtils.isBlank(clientIdOriginal)) {
        connAckProperties.add(new MqttProperties.StringProperty(MqttProperties.MqttPropertyType.ASSIGNED_CLIENT_IDENTIFIER.value(), mqttEndpoint.clientIdentifier()));
      }
      // TODO When these MQTT 5 features implemented, change 0 to 1
      connAckProperties.add(new MqttProperties.IntegerProperty(MqttProperties.MqttPropertyType.SUBSCRIPTION_IDENTIFIER_AVAILABLE.value(), 0));
      connAckProperties.add(new MqttProperties.IntegerProperty(MqttProperties.MqttPropertyType.SHARED_SUBSCRIPTION_AVAILABLE.value(), 0));
    }

    Context context = Context.empty();
    Uni.createFrom().voidItem()
      .onItem().transformToUni(v -> clientService.obtainClientLock(mqttEndpoint.clientIdentifier(), 2000))
      .onItem().transformToUni(v -> authenticate(mqttEndpoint))
      .onItem().transformToUni(v -> kickOffExistingConnection(mqttEndpoint, context))
      .onItem().transformToUni(v -> registerHandler(mqttEndpoint))
      .onItem().transformToUni(v -> computeSessionPresent(mqttEndpoint, context))
      .onItem().transformToUni(v -> deployClientVerticle(mqttEndpoint))
      .onItem().transformToUni(clientVerticleId -> handleSession(mqttEndpoint, clientVerticleId))
      .onItem().call(session -> publishMqttSessionTakenOverEvent(session, context))
      .onItem().transformToUni(session -> handleWill(mqttEndpoint, session))
      // Publish EVENT_MQTT_CONNECTED_EVENT
      .onItem().call(v -> publishMqttConnectedEvent(mqttEndpoint))
      .eventually(() -> clientService.releaseClientLock(mqttEndpoint.clientIdentifier()))
      .subscribe().with(context, v -> {
        boolean sessionPresent = getSessionPresentFromContext(context);
        if (mqttEndpoint.protocolVersion() <= MqttVersion.MQTT_3_1_1.protocolLevel()) {
          mqttEndpoint.accept(sessionPresent);
        } else {
          mqttEndpoint.accept(sessionPresent, connAckProperties);
        }
        if (LOGGER.isDebugEnabled()) {
          LOGGER.debug("Mqtt client {} connected", mqttEndpoint.clientIdentifier());
        }
      }, t -> {
        LOGGER.error("Error occurred when processing CONNECT from " + mqttEndpoint.clientIdentifier(), t);
        publishMqttConnectFailedEvent(mqttEndpoint, t).subscribe().with(v -> {}, tt -> LOGGER.error("Error occurred when publishing mqtt connect failed event", tt));
        if (mqttEndpoint.protocolVersion() <= MqttVersion.MQTT_3_1_1.protocolLevel()) {
          if (t instanceof MqttAuthFailedException e) {
            mqttEndpoint.reject(e.getCode());
          } else {
            mqttEndpoint.reject(MqttConnectReturnCode.CONNECTION_REFUSED_SERVER_UNAVAILABLE);
          }
        } else {
          if (t instanceof MqttAuthFailedException e) {
            connAckProperties.add(new MqttProperties.StringProperty(MqttProperties.MqttPropertyType.REASON_STRING.value(), ((MqttAuthFailedException) t).getReason()));
            mqttEndpoint.reject(e.getCode(), connAckProperties);
          } else {
            connAckProperties.add(new MqttProperties.StringProperty(MqttProperties.MqttPropertyType.REASON_STRING.value(), t.getMessage()));
            mqttEndpoint.reject(MqttConnectReturnCode.CONNECTION_REFUSED_UNSPECIFIED_ERROR, connAckProperties);
          }
        }
      });

  }

  /**
   * Compute CONNECT info.
   *
   * @param mqttEndpoint mqttEndpoint
   * @return CONNECT info.
   */
  private String connectInfo(MqttEndpoint mqttEndpoint) {
    JsonObject jsonObject = new JsonObject();
    jsonObject.put("protocolName", mqttEndpoint.protocolName());
    jsonObject.put("protocolVersion", mqttEndpoint.protocolVersion());
    jsonObject.put("clientIdentifier", mqttEndpoint.clientIdentifier());
    jsonObject.put("auth", mqttEndpoint.auth() == null ? null : mqttEndpoint.auth().toJson());
    jsonObject.put("keepAliveTimeSeconds", mqttEndpoint.keepAliveTimeSeconds());
    jsonObject.put("cleanSession", mqttEndpoint.isCleanSession());
    jsonObject.put("connectProperties", MqttPropertiesUtil.encode(mqttEndpoint.connectProperties()));
    jsonObject.put("will", mqttEndpoint.will().toJson());
    jsonObject.put("remoteHost", mqttEndpoint.remoteAddress().host());
    jsonObject.put("remotePort", mqttEndpoint.remoteAddress().port());
    return jsonObject.toString();
  }

  /**
   * Authenticate.
   *
   * @param mqttEndpoint mqttEndpoint
   * @return If the authentication fails, an MqttConnectFailedException is thrown.
   */
  private Uni<Void> authenticate(MqttEndpoint mqttEndpoint) {
    MqttAuth mqttAuth = mqttEndpoint.auth();
    MqttAuthData mqttAuthData = MqttAuthData.builder()
      .protocolLevel(mqttEndpoint.protocolVersion())
      .clientId(mqttEndpoint.clientIdentifier())
      .username(mqttAuth == null ? null : mqttAuth.getUsername())
      .password(mqttAuth == null ? null : mqttAuth.getPassword().getBytes(StandardCharsets.UTF_8))
      .build();
    LOGGER.debug("Mqtt auth data: {}", mqttAuthData.toJson());
    return authenticationService.authenticate(mqttAuthData)
      .onItem().invoke(mqttAuthResult -> LOGGER.debug("Mqtt auth result: {}", mqttAuthResult.toJson()))
      .onItem().transformToUni(mqttAuthResult -> {
        if (MqttConnectReturnCode.CONNECTION_ACCEPTED.equals(mqttAuthResult.getCode())) {
          return Uni.createFrom().voidItem();
        } else {
          return Uni.createFrom().failure(new MqttAuthFailedException(mqttAuthResult.getCode(), mqttAuthResult.getReason()));
        }
      });
  }

  /**
   * Kick off existing connection.
   *
   * @param mqttEndpoint mqttEndpoint
   * @return Void
   */
  private Uni<Void> kickOffExistingConnection(MqttEndpoint mqttEndpoint, Context context) {
    return Uni.createFrom().emitter(uniEmitter ->
      sessionService.getSession(mqttEndpoint.clientIdentifier())
        .onItem().transformToUni(session -> {
          if (session != null && session.isOnline() && StringUtils.isNotBlank(session.getVerticleId())) {
            LOGGER.warn("Kick off existing connection for: {}", mqttEndpoint.clientIdentifier());
            // Whether the above code can receive EVENT_MQTT_ENDPOINT_CLOSED_EVENT, always continue to run forward after a period of time.
            long timerId = vertx.setTimer(3000, l -> uniEmitter.complete(null));
            AtomicReference<MessageConsumer<JsonObject>> messageConsumer = new AtomicReference<>();
            return Uni.createFrom().voidItem()
              .onItem().invoke(() -> clientService.releaseClientLock(mqttEndpoint.clientIdentifier()))
              .onItem().transformToUni(v -> eventService
                .consumeEvent(EventType.EVENT_MQTT_ENDPOINT_CLOSED, data -> {
                  if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Consuming event: {}", data);
                  }
                  MqttEndpointClosedEvent mqttEndpointClosedEvent = new MqttEndpointClosedEvent().fromJson(data);
                  if (session.getSessionId().equals(mqttEndpointClosedEvent.getSessionId())) {
                    // When EVENT_MQTT_ENDPOINT_CLOSED_EVENT received and sessionId is same, cancel timer and run forward.
                    vertx.cancelTimer(timerId);
                    uniEmitter.complete(null);
                  }
                  messageConsumer.get().unregisterAndForget();
                }, false))
              .onItem().invoke(messageConsumer::set)
              .onItem().transformToUni(v -> {
                if (mqttEndpoint.protocolVersion() <= MqttVersion.MQTT_3_1_1.protocolLevel()) {
                  return clientService.closeMqttEndpoint(session.getVerticleId());
                } else {
                  return clientService.disconnect(session.getVerticleId(), new DisconnectRequest(MqttDisconnectReasonCode.SESSION_TAKEN_OVER, MqttProperties.NO_PROPERTIES));
                }
              })
              .onItem().invoke(() -> {
                context.put(CONTEXT_KEY_SESSION_TAKEN_OVER, true);
                context.put(CONTEXT_KEY_SESSION_TAKEN_OVER_OLD_SESSION, session);
              });
          } else {
            uniEmitter.complete(null);
            return Uni.createFrom().voidItem();
          }
        })
        .subscribe().with(ConsumerUtil.nothingToDo(), uniEmitter::fail)
    );
  }

  /**
   * Register mqtt handlers.
   *
   * @param mqttEndpoint mqttEndpoint
   * @return Void
   */
  private Uni<Void> registerHandler(MqttEndpoint mqttEndpoint) {
    mqttEndpoint.disconnectMessageHandler(new MqttDisconnectMessageHandler(mqttEndpoint, vertx, sessionService, willService, eventService));
    mqttEndpoint.closeHandler(new MqttCloseHandler(mqttEndpoint, vertx, clientService, compositeService, sessionService, willService, eventService));
    mqttEndpoint.pingHandler(new MqttPingHandler(mqttEndpoint, vertx, sessionService, eventService));
    mqttEndpoint.exceptionHandler(new MqttExceptionHandler(mqttEndpoint, vertx, eventService));
    mqttEndpoint.subscribeHandler(new MqttSubscribeHandler(mqttEndpoint, vertx, subService, sessionService, retainService, compositeService, eventService));
    mqttEndpoint.unsubscribeHandler(new MqttUnsubscribeHandler(mqttEndpoint, vertx, sessionService, subService, eventService));
    mqttEndpoint.publishHandler(new MqttPublishHandler(mqttEndpoint, vertx, msgService, sessionService, retainService, compositeService, eventService));
    mqttEndpoint.publishReleaseMessageHandler(new MqttPublishReleaseMessageHandler(mqttEndpoint, sessionService, msgService, compositeService));
    mqttEndpoint.publishAcknowledgeMessageHandler(new MqttPublishAcknowledgeMessageHandler(mqttEndpoint, sessionService, msgService, eventService, vertx));
    mqttEndpoint.publishReceivedMessageHandler(new MqttPublishReceivedMessageHandler(mqttEndpoint, sessionService, msgService, eventService, vertx));
    mqttEndpoint.publishCompletionMessageHandler(new MqttPublishCompletionMessageHandler(mqttEndpoint, sessionService, msgService));
    return Uni.createFrom().voidItem();
  }

  /**
   * Compute sessionPresent and store in context
   *
   * @param mqttEndpoint mqttEndpoint
   * @param context      context
   * @return Void
   */
  private Uni<Void> computeSessionPresent(MqttEndpoint mqttEndpoint, Context context) {
    if (mqttEndpoint.isCleanSession()) {
      context.put(CONTEXT_KEY_SESSION_PRESENT, false);
      return Uni.createFrom().voidItem();
    } else {
      return sessionService.getSession(mqttEndpoint.clientIdentifier())
        .onItem().invoke(session -> context.put(CONTEXT_KEY_SESSION_PRESENT, Objects.nonNull(session)))
        .replaceWithVoid();
    }
  }

  /**
   * Get sessionPresent from context
   *
   * @return sessionPresent
   */
  private boolean getSessionPresentFromContext(Context context) {
    return context.get(CONTEXT_KEY_SESSION_PRESENT);
  }

  /**
   * Deploy client verticle
   *
   * @param mqttEndpoint mqttEndpoint
   * @return Client verticle deployment id;
   */
  private Uni<String> deployClientVerticle(MqttEndpoint mqttEndpoint) {
    ClientVerticle clientVerticle = new ClientVerticle(mqttEndpoint, sessionService, msgService);
    return vertx.deployVerticle(clientVerticle, new DeploymentOptions());
  }

  /**
   * Handle session
   *
   * @param mqttEndpoint     mqttEndpoint
   * @param clientVerticleId clientVerticleId
   * @return Session
   */
  private Uni<Session> handleSession(MqttEndpoint mqttEndpoint, String clientVerticleId) {
    String nodeId = VertxUtil.getNodeId(vertx);
    Integer sessionExpiryInterval;
    if (mqttEndpoint.protocolVersion() <= MqttVersion.MQTT_3_1_1.protocolLevel()) {
      sessionExpiryInterval = null;
    } else {
      MqttProperties.MqttProperty sessionExpiryIntervalMqttProperty = mqttEndpoint.connectProperties().getProperty(MqttProperties.MqttPropertyType.SESSION_EXPIRY_INTERVAL.value());
      sessionExpiryInterval = sessionExpiryIntervalMqttProperty == null ? null : (Integer) sessionExpiryIntervalMqttProperty.value();
    }
    Instant now = Instant.now();
    return sessionService.getSession(mqttEndpoint.clientIdentifier()).onItem().transformToUni(previousSession -> {
      if (mqttEndpoint.isCleanSession()) {
        return Uni.createFrom().voidItem()
          .onItem().transformToUni(v -> {
            if (previousSession != null) {
              return compositeService.clearSessionData(mqttEndpoint.clientIdentifier());
            } else {
              return Uni.createFrom().voidItem();
            }
          }).onItem().transformToUni(v -> {
            Session newSession = new Session().setSessionId(UUIDUtil.timeBasedUuid().toString())
              .setClientId(mqttEndpoint.clientIdentifier()).setOnline(true).setVerticleId(clientVerticleId).setNodeId(nodeId)
              .setCleanSession(true).setKeepAlive(mqttEndpoint.keepAliveTimeSeconds())
              .setProtocolLevel(mqttEndpoint.protocolVersion()).setSessionExpiryInterval(sessionExpiryInterval)
              .setCreatedTime(now.toEpochMilli()).setUpdatedTime(now.toEpochMilli());
            return sessionService.saveOrUpdateSession(newSession).replaceWith(newSession);
          });
      } else {
        if (previousSession != null) {
          // Resend OutboundQos1Pub OutboundQos2Pub OutboundQos2Rel OfflineMsg
          vertx.setTimer(500, l -> {
            resendOutboundQos1Pub(mqttEndpoint, previousSession.getSessionId()).subscribe().with(ConsumerUtil.nothingToDo(), t -> LOGGER.error("Error occurred when sending outboundQos1Pub", t));
            resendOutboundQos2Pub(mqttEndpoint, previousSession.getSessionId()).subscribe().with(ConsumerUtil.nothingToDo(), t -> LOGGER.error("Error occurred when sending outboundQos2Pub", t));
            resendOutboundQos2Rel(mqttEndpoint, previousSession.getSessionId()).subscribe().with(ConsumerUtil.nothingToDo(), t -> LOGGER.error("Error occurred when sending outboundQos2Rel", t));
          });
          vertx.setTimer(1000, l -> compositeService.sendOfflineMsg(previousSession.getSessionId())
            .subscribe().with(ConsumerUtil.nothingToDo(), t -> LOGGER.error("Error occurred when sending offline messages of " + mqttEndpoint.clientIdentifier(), t)));

          Session updatedSession = previousSession.copy().setOnline(true).setVerticleId(clientVerticleId).setNodeId(nodeId)
            .setCleanSession(false).setProtocolLevel(mqttEndpoint.protocolVersion()).setSessionExpiryInterval(sessionExpiryInterval)
            .setUpdatedTime(now.toEpochMilli());
          return sessionService.saveOrUpdateSession(updatedSession).replaceWith(updatedSession);
        } else {
          Session newSession = new Session().setSessionId(UUIDUtil.timeBasedUuid().toString())
            .setClientId(mqttEndpoint.clientIdentifier()).setOnline(true).setVerticleId(clientVerticleId).setNodeId(nodeId)
            .setCleanSession(false).setKeepAlive(mqttEndpoint.keepAliveTimeSeconds())
            .setProtocolLevel(mqttEndpoint.protocolVersion()).setSessionExpiryInterval(sessionExpiryInterval)
            .setCreatedTime(now.toEpochMilli()).setUpdatedTime(now.toEpochMilli());
          return sessionService.saveOrUpdateSession(newSession).replaceWith(newSession);
        }
      }
    });
  }

  public Uni<Void> publishMqttSessionTakenOverEvent(Session newSession, Context context){
    boolean sessionTakenOver = context.getOrElse(CONTEXT_KEY_SESSION_TAKEN_OVER, () -> false);
    if (sessionTakenOver){
      Session oldSession = context.get(CONTEXT_KEY_SESSION_TAKEN_OVER_OLD_SESSION);
      String oldSessionId = oldSession != null ? oldSession.getSessionId() : "";
      Event event = new MqttSessionTakenOverEvent(Instant.now().toEpochMilli(), VertxUtil.getNodeId(vertx), newSession.getClientId(), oldSessionId, newSession.getSessionId());
      if (LOGGER.isDebugEnabled()) {
        LOGGER.debug("Publishing event: {}, ", event.toJson());
      }
      return eventService.publishEvent(event);
    } else {
      return Uni.createFrom().voidItem();
    }
  }

  /**
   * Handle will
   *
   * @param mqttEndpoint mqttEndpoint
   * @param session      session
   * @return Void
   */
  private Uni<Void> handleWill(MqttEndpoint mqttEndpoint, Session session) {
    if (mqttEndpoint.will().isWillFlag()) {
      Will will;
      if (mqttEndpoint.protocolVersion() <= MqttVersion.MQTT_3_1_1.protocolLevel()) {
        will = new Will().setSessionId(session.getSessionId()).setClientId(mqttEndpoint.clientIdentifier()).setWillTopicName(mqttEndpoint.will().getWillTopic())
          .setWillMessage(mqttEndpoint.will().getWillMessage()).setWillQos(mqttEndpoint.will().getWillQos()).setWillRetain(mqttEndpoint.will().isWillRetain())
          .setCreatedTime(Instant.now().toEpochMilli());
      } else {
        MqttProperties.MqttProperty willDelayIntervalProperty = mqttEndpoint.connectProperties().getProperty(MqttProperties.MqttPropertyType.WILL_DELAY_INTERVAL.value());
        Integer willDelayInterval = willDelayIntervalProperty == null ? null : (Integer) willDelayIntervalProperty.value();
        MqttProperties.MqttProperty payloadFormatIndicatorProperty = mqttEndpoint.connectProperties().getProperty(MqttProperties.MqttPropertyType.PAYLOAD_FORMAT_INDICATOR.value());
        Integer payloadFormatIndicator = payloadFormatIndicatorProperty == null ? null : (Integer) payloadFormatIndicatorProperty.value();
        MqttProperties.MqttProperty messageExpiryIntervalProperty = mqttEndpoint.connectProperties().getProperty(MqttProperties.MqttPropertyType.PUBLICATION_EXPIRY_INTERVAL.value());
        Integer messageExpiryInterval = messageExpiryIntervalProperty == null ? null : (Integer) messageExpiryIntervalProperty.value();
        MqttProperties.MqttProperty contentTypeProperty = mqttEndpoint.connectProperties().getProperty(MqttProperties.MqttPropertyType.CONTENT_TYPE.value());
        String contentType = contentTypeProperty == null ? null : (String) contentTypeProperty.value();
        MqttProperties.MqttProperty responseTopicProperty = mqttEndpoint.connectProperties().getProperty(MqttProperties.MqttPropertyType.RESPONSE_TOPIC.value());
        String responseTopic = responseTopicProperty == null ? null : (String) responseTopicProperty.value();
        MqttProperties.MqttProperty correlationDataProperty = mqttEndpoint.connectProperties().getProperty(MqttProperties.MqttPropertyType.CORRELATION_DATA.value());
        Buffer correlationData = correlationDataProperty == null ? null : Buffer.buffer((byte[]) correlationDataProperty.value());
        List<? extends MqttProperties.MqttProperty> userPropertiesProperty = mqttEndpoint.connectProperties().getProperties(MqttProperties.MqttPropertyType.USER_PROPERTY.value());
        List<StringPair> userProperties = userPropertiesProperty.stream().map(mqttProperty -> (MqttProperties.UserProperty) mqttProperty).map(userProperty -> new StringPair(userProperty.value().key, userProperty.value().value)).collect(Collectors.toList());

        will = new Will().setSessionId(session.getSessionId()).setClientId(mqttEndpoint.clientIdentifier()).setWillTopicName(mqttEndpoint.will().getWillTopic())
          .setWillMessage(mqttEndpoint.will().getWillMessage()).setWillQos(mqttEndpoint.will().getWillQos()).setWillRetain(mqttEndpoint.will().isWillRetain())
          .setWillDelayInterval(willDelayInterval).setPayloadFormatIndicator(payloadFormatIndicator).setMessageExpiryInterval(messageExpiryInterval)
          .setContentType(contentType).setResponseTopic(responseTopic).setCorrelationData(correlationData).setUserProperties(userProperties)
          .setCreatedTime(Instant.now().toEpochMilli());
      }
      return willService.saveOrUpdateWill(will);
    } else {
      return Uni.createFrom().voidItem();
    }
  }

  private Uni<Void> publishMqttConnectedEvent(MqttEndpoint mqttEndpoint) {
    Event event = new MqttConnectedEvent(Instant.now().toEpochMilli(), VertxUtil.getNodeId(vertx),
      mqttEndpoint.clientIdentifier(), mqttEndpoint.protocolVersion(),
      mqttEndpoint.auth() != null ? mqttEndpoint.auth().getUsername() : "",
      mqttEndpoint.auth() != null ? mqttEndpoint.auth().getPassword() : "");
    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("Publishing event: {}, ", event.toJson());
    }
    return eventService.publishEvent(event);
  }

  private Uni<Void> publishMqttConnectFailedEvent(MqttEndpoint mqttEndpoint, Throwable t) {
    Event event = new MqttConnectFailedEvent(Instant.now().toEpochMilli(), VertxUtil.getNodeId(vertx),
      mqttEndpoint.clientIdentifier(), t.getMessage());
    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("Publishing event: {}, ", event.toJson());
    }
    return eventService.publishEvent(event);
  }

  private Uni<Void> resendOutboundQos1Pub(MqttEndpoint mqttEndpoint, String sessionId) {
    return Uni.createFrom().emitter(uniEmitter -> msgService.outboundQos1Pub(sessionId)
      .onItem().transformToMulti(outboundQos1Pubs -> Multi.createFrom().items(outboundQos1Pubs.stream()))
      .onItem().call(outboundQos1Pub -> {
        if (mqttEndpoint.protocolVersion() <= MqttVersion.MQTT_3_1_1.protocolLevel()) {
          return mqttEndpoint.publish(outboundQos1Pub.getTopic(), io.vertx.mutiny.core.buffer.Buffer.newInstance(outboundQos1Pub.getPayload()),
            MqttQoS.valueOf(outboundQos1Pub.getQos()), true, outboundQos1Pub.isRetain(), outboundQos1Pub.getMessageId()).replaceWithVoid();
        } else {
          return mqttEndpoint.publish(outboundQos1Pub.getTopic(), io.vertx.mutiny.core.buffer.Buffer.newInstance(outboundQos1Pub.getPayload()),
            MqttQoS.valueOf(outboundQos1Pub.getQos()), true, outboundQos1Pub.isRetain(), outboundQos1Pub.getMessageId(),
            MqttProperties.NO_PROPERTIES).replaceWithVoid();
        }
      }).subscribe().with(ConsumerUtil.nothingToDo(), uniEmitter::fail, () -> uniEmitter.complete(null)));
  }

  private Uni<Void> resendOutboundQos2Pub(MqttEndpoint mqttEndpoint, String sessionId) {
    return Uni.createFrom().emitter(uniEmitter -> msgService.outboundQos2Pub(sessionId)
      .onItem().transformToMulti(outboundQos2Pubs -> Multi.createFrom().items(outboundQos2Pubs.stream()))
      .onItem().call(outboundQos2Pub -> {
        if (mqttEndpoint.protocolVersion() <= MqttVersion.MQTT_3_1_1.protocolLevel()) {
          return mqttEndpoint.publish(outboundQos2Pub.getTopic(), io.vertx.mutiny.core.buffer.Buffer.newInstance(outboundQos2Pub.getPayload()),
            MqttQoS.valueOf(outboundQos2Pub.getQos()), true, outboundQos2Pub.isRetain(), outboundQos2Pub.getMessageId()).replaceWithVoid();
        } else {
          return mqttEndpoint.publish(outboundQos2Pub.getTopic(), io.vertx.mutiny.core.buffer.Buffer.newInstance(outboundQos2Pub.getPayload()),
            MqttQoS.valueOf(outboundQos2Pub.getQos()), true, outboundQos2Pub.isRetain(), outboundQos2Pub.getMessageId(),
            MqttProperties.NO_PROPERTIES).replaceWithVoid();
        }
      }).subscribe().with(ConsumerUtil.nothingToDo(), uniEmitter::fail, () -> uniEmitter.complete(null)));
  }

  private Uni<Void> resendOutboundQos2Rel(MqttEndpoint mqttEndpoint, String sessionId) {
    return Uni.createFrom().emitter(uniEmitter -> msgService.outboundQos2Rel(sessionId)
      .onItem().transformToMulti(outboundQos2Rels -> Multi.createFrom().items(outboundQos2Rels.stream()))
      .onItem().invoke(outboundQos2Rel -> {
        if (mqttEndpoint.protocolVersion() <= MqttVersion.MQTT_3_1_1.protocolLevel()) {
          mqttEndpoint.publishRelease(outboundQos2Rel.getMessageId());
        } else {
          mqttEndpoint.publishRelease(outboundQos2Rel.getMessageId(), MqttPubRelReasonCode.SUCCESS, MqttProperties.NO_PROPERTIES);
        }
      }).subscribe().with(ConsumerUtil.nothingToDo(), uniEmitter::fail, () -> uniEmitter.complete(null)));
  }

}
