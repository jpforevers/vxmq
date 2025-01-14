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

import io.github.jpforevers.vxmq.event.Event;
import io.github.jpforevers.vxmq.event.EventService;
import io.github.jpforevers.vxmq.event.EventType;
import io.github.jpforevers.vxmq.event.mqtt.MqttConnectFailedEvent;
import io.github.jpforevers.vxmq.event.mqtt.MqttConnectedEvent;
import io.github.jpforevers.vxmq.event.mqtt.MqttEndpointClosedEvent;
import io.github.jpforevers.vxmq.event.mqtt.MqttSessionTakenOverEvent;
import io.github.jpforevers.vxmq.mqtt.exception.MqttConnectException;
import io.github.jpforevers.vxmq.service.alias.InboundTopicAliasService;
import io.github.jpforevers.vxmq.service.alias.OutboundTopicAliasService;
import io.github.jpforevers.vxmq.service.authentication.MqttAuthData;
import io.github.jpforevers.vxmq.service.authentication.mutiny.AuthenticationService;
import io.github.jpforevers.vxmq.service.client.ClientService;
import io.github.jpforevers.vxmq.service.client.ClientVerticle;
import io.github.jpforevers.vxmq.service.client.CloseMqttEndpointRequest;
import io.github.jpforevers.vxmq.service.client.DisconnectRequest;
import io.github.jpforevers.vxmq.service.composite.CompositeService;
import io.github.jpforevers.vxmq.service.flow.FlowControlService;
import io.github.jpforevers.vxmq.service.msg.MsgService;
import io.github.jpforevers.vxmq.service.retain.RetainService;
import io.github.jpforevers.vxmq.service.session.Session;
import io.github.jpforevers.vxmq.service.session.SessionService;
import io.github.jpforevers.vxmq.service.sub.mutiny.SubService;
import io.github.jpforevers.vxmq.service.will.Will;
import io.github.jpforevers.vxmq.service.will.WillService;
import io.github.jpforevers.vxmq.assist.*;
import io.micrometer.core.instrument.Counter;
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
import java.util.regex.Pattern;

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
  private final InboundTopicAliasService inboundTopicAliasService;
  private final OutboundTopicAliasService outboundTopicAliasService;
  private final Counter packetsPublishReceivedCounter;
  private final Counter packetsPublishSentCounter;
  private final int inboundReceiveMaximum;
  private final FlowControlService flowControlService;

  public MqttEndpointHandler(Vertx vertx,
                             SessionService sessionService,
                             MsgService msgService,
                             WillService willService,
                             ClientService clientService,
                             SubService subService,
                             RetainService retainService,
                             CompositeService compositeService,
                             EventService eventService,
                             AuthenticationService authenticationService,
                             InboundTopicAliasService inboundTopicAliasService,
                             OutboundTopicAliasService outboundTopicAliasService,
                             Counter packetsPublishReceivedCounter,
                             Counter packetsPublishSentCounter,
                             int inboundReceiveMaximum,
                             FlowControlService flowControlService) {
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
    this.inboundTopicAliasService = inboundTopicAliasService;
    this.outboundTopicAliasService = outboundTopicAliasService;
    this.packetsPublishReceivedCounter = packetsPublishReceivedCounter;
    this.packetsPublishSentCounter = packetsPublishSentCounter;
    this.inboundReceiveMaximum = inboundReceiveMaximum;
    this.flowControlService = flowControlService;
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

    MqttProperties conAckMqttProperties = new MqttProperties();
    if (mqttEndpoint.protocolVersion() > MqttVersion.MQTT_3_1_1.protocolLevel()) {
      if (StringUtils.isBlank(clientIdOriginal)) {
        conAckMqttProperties.add(new MqttProperties.StringProperty(MqttProperties.MqttPropertyType.ASSIGNED_CLIENT_IDENTIFIER.value(), mqttEndpoint.clientIdentifier()));
      }
      conAckMqttProperties.add(new MqttProperties.IntegerProperty(MqttProperties.MqttPropertyType.SUBSCRIPTION_IDENTIFIER_AVAILABLE.value(), 1));
      conAckMqttProperties.add(new MqttProperties.IntegerProperty(MqttProperties.MqttPropertyType.SHARED_SUBSCRIPTION_AVAILABLE.value(), 1));
      conAckMqttProperties.add(new MqttProperties.IntegerProperty(MqttProperties.MqttPropertyType.TOPIC_ALIAS_MAXIMUM.value(), Config.getMqttTopicAliasMax()));
      conAckMqttProperties.add(new MqttProperties.IntegerProperty(MqttProperties.MqttPropertyType.RECEIVE_MAXIMUM.value(), inboundReceiveMaximum));
    }

    Context context = Context.empty();
    Uni.createFrom().voidItem()
      .onItem().transformToUni(v -> clientService.obtainClientLock(mqttEndpoint.clientIdentifier(), 2000))
      .onItem().transformToUni(v -> checkClientId(mqttEndpoint.clientIdentifier()))
      .onItem().transformToUni(v -> authenticate(mqttEndpoint))
      .onItem().transformToUni(v -> kickOffExistingConnection(mqttEndpoint, context))
      .onItem().transformToUni(v -> registerHandler(mqttEndpoint))
      .onItem().transformToUni(v -> computeSessionPresent(mqttEndpoint, context))
      .onItem().transformToUni(v -> deployClientVerticle(mqttEndpoint, packetsPublishSentCounter))
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
          mqttEndpoint.accept(sessionPresent, conAckMqttProperties);
        }
        if (LOGGER.isDebugEnabled()) {
          LOGGER.debug("Mqtt client {} connected", mqttEndpoint.clientIdentifier());
        }
      }, t -> {
        LOGGER.error("Error occurred when processing CONNECT from {}", mqttEndpoint.clientIdentifier(), t);
        publishMqttConnectFailedEvent(mqttEndpoint, t).subscribe().with(v -> {}, tt -> LOGGER.error("Error occurred when publishing mqtt connect failed event", tt));
        if (mqttEndpoint.protocolVersion() <= MqttVersion.MQTT_3_1_1.protocolLevel()) {
          if (t instanceof MqttConnectException e) {
            mqttEndpoint.reject(e.getMqttConnectReturnCode());
          } else {
            mqttEndpoint.reject(MqttConnectReturnCode.CONNECTION_REFUSED_SERVER_UNAVAILABLE);
          }
        } else {
          conAckMqttProperties.add(new MqttProperties.StringProperty(MqttProperties.MqttPropertyType.REASON_STRING.value(), t.getMessage()));
          if (t instanceof MqttConnectException e) {
            mqttEndpoint.reject(e.getMqttConnectReturnCode(), conAckMqttProperties);
          } else {
            mqttEndpoint.reject(MqttConnectReturnCode.CONNECTION_REFUSED_UNSPECIFIED_ERROR, conAckMqttProperties);
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

  private Uni<Void> checkClientId(String clientIdentifier) {
    int clientIdLengthMax = Config.getMqttClientIdLengthMax();
    if (clientIdentifier.length() > clientIdLengthMax) {
      return Uni.createFrom().failure(new MqttConnectException(MqttConnectReturnCode.CONNECTION_REFUSED_CLIENT_IDENTIFIER_NOT_VALID, "Client identifier is too long, exceeding " + clientIdLengthMax));
    } else {
      return Uni.createFrom().voidItem();
    }
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
    if (ifInWhitelist(mqttEndpoint.clientIdentifier())) {
      LOGGER.debug("Client id {} in white list, skip authenticate", mqttEndpoint.clientIdentifier());
      return Uni.createFrom().voidItem();
    } else {
      return authenticationService.authenticate(mqttAuthData)
        .onItem().invoke(mqttAuthResult -> LOGGER.debug("Mqtt auth result: {}", mqttAuthResult.toJson()))
        .onItem().transformToUni(mqttAuthResult -> {
          if (MqttConnectReturnCode.CONNECTION_ACCEPTED.equals(mqttAuthResult.getCode())) {
            return Uni.createFrom().voidItem();
          } else {
            return Uni.createFrom().failure(new MqttConnectException(mqttAuthResult.getCode(), mqttAuthResult.getReason()));
          }
        });
    }
  }

  private boolean ifInWhitelist(String clientIdentifier) {
    for (String s : Config.getMqttAuthWhitelist()) {
      if (Pattern.matches(s, clientIdentifier)) {
        return true;
      }
    }
    return false;
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
                if (session.getProtocolLevel() <= MqttVersion.MQTT_3_1_1.protocolLevel()) {
                  return clientService.closeMqttEndpoint(session.getVerticleId(), new CloseMqttEndpointRequest());
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
    mqttEndpoint.closeHandler(new MqttCloseHandler(mqttEndpoint, vertx, clientService, compositeService, sessionService, willService, eventService, inboundTopicAliasService, outboundTopicAliasService, flowControlService));
    mqttEndpoint.pingHandler(new MqttPingHandler(mqttEndpoint, vertx, sessionService, eventService));
    mqttEndpoint.exceptionHandler(new MqttExceptionHandler(mqttEndpoint, vertx, eventService));
    mqttEndpoint.subscribeHandler(new MqttSubscribeHandler(mqttEndpoint, vertx, subService, sessionService, retainService, compositeService, eventService));
    mqttEndpoint.unsubscribeHandler(new MqttUnsubscribeHandler(mqttEndpoint, vertx, sessionService, subService, eventService));
    mqttEndpoint.publishHandler(new MqttPublishHandler(mqttEndpoint, vertx, msgService, sessionService, retainService, compositeService, eventService, packetsPublishReceivedCounter, inboundReceiveMaximum, flowControlService));
    mqttEndpoint.publishReleaseMessageHandler(new MqttPublishReleaseMessageHandler(mqttEndpoint, sessionService, msgService, compositeService, flowControlService));
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
  private Uni<String> deployClientVerticle(MqttEndpoint mqttEndpoint, Counter packetsPublishSentCounter) {
    ClientVerticle clientVerticle = new ClientVerticle(mqttEndpoint, sessionService, msgService, outboundTopicAliasService, packetsPublishSentCounter);
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
      // From MQTT 5 specification: If the Session Expiry Interval is absent the value 0 is used
      sessionExpiryInterval = sessionExpiryIntervalMqttProperty == null ? 0 : (Integer) sessionExpiryIntervalMqttProperty.value();
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
        Integer willDelayInterval = MqttPropertiesUtil.getValue(mqttEndpoint.connectProperties(), MqttProperties.MqttPropertyType.WILL_DELAY_INTERVAL, MqttProperties.IntegerProperty.class);
        Integer payloadFormatIndicator = MqttPropertiesUtil.getValue(mqttEndpoint.connectProperties(), MqttProperties.MqttPropertyType.PAYLOAD_FORMAT_INDICATOR, MqttProperties.IntegerProperty.class);
        Integer messageExpiryInterval = MqttPropertiesUtil.getValue(mqttEndpoint.connectProperties(), MqttProperties.MqttPropertyType.PUBLICATION_EXPIRY_INTERVAL, MqttProperties.IntegerProperty.class);
        String contentType = MqttPropertiesUtil.getValue(mqttEndpoint.connectProperties(), MqttProperties.MqttPropertyType.CONTENT_TYPE, MqttProperties.StringProperty.class);
        String responseTopic = MqttPropertiesUtil.getValue(mqttEndpoint.connectProperties(), MqttProperties.MqttPropertyType.RESPONSE_TOPIC, MqttProperties.StringProperty.class);
        byte[] correlationDataBytes = MqttPropertiesUtil.getValue(mqttEndpoint.connectProperties(), MqttProperties.MqttPropertyType.CORRELATION_DATA, MqttProperties.BinaryProperty.class);
        Buffer correlationData = correlationDataBytes != null ? Buffer.buffer(correlationDataBytes) : null;
        List<MqttProperties.StringPair> userProperties = MqttPropertiesUtil.getValues(mqttEndpoint.connectProperties(), MqttProperties.MqttPropertyType.USER_PROPERTY, MqttProperties.UserProperty.class);

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
              MqttQoS.valueOf(outboundQos1Pub.getQos()), true, outboundQos1Pub.isRetain(), outboundQos1Pub.getMessageId())
            .replaceWithVoid();
        } else {
          MqttProperties mqttProperties = new MqttProperties();
          if (outboundQos1Pub.getMessageExpiryInterval() != null && outboundQos1Pub.getMessageExpiryInterval() != 0) {
            long messageExpiryInterval = outboundQos1Pub.getMessageExpiryInterval() - (Instant.now().toEpochMilli() - outboundQos1Pub.getCreatedTime()) / 1000;
            mqttProperties.add(new MqttProperties.IntegerProperty(MqttProperties.MqttPropertyType.PUBLICATION_EXPIRY_INTERVAL.value(), (int) messageExpiryInterval));
          }
          if (outboundQos1Pub.getPayloadFormatIndicator() != null) {
            mqttProperties.add(new MqttProperties.IntegerProperty(MqttProperties.MqttPropertyType.PAYLOAD_FORMAT_INDICATOR.value(), outboundQos1Pub.getPayloadFormatIndicator()));
          }
          if (StringUtils.isNotBlank(outboundQos1Pub.getContentType())) {
            mqttProperties.add(new MqttProperties.StringProperty(MqttProperties.MqttPropertyType.CONTENT_TYPE.value(), outboundQos1Pub.getContentType()));
          }
          if (StringUtils.isNotBlank(outboundQos1Pub.getResponseTopic())) {
            mqttProperties.add(new MqttProperties.StringProperty(MqttProperties.MqttPropertyType.RESPONSE_TOPIC.value(), outboundQos1Pub.getResponseTopic()));
          }
          if (outboundQos1Pub.getCorrelationData() != null) {
            mqttProperties.add(new MqttProperties.BinaryProperty(MqttProperties.MqttPropertyType.CORRELATION_DATA.value(), outboundQos1Pub.getCorrelationData().getBytes()));
          }
          if (outboundQos1Pub.getSubscriptionIdentifier() != null) {
            mqttProperties.add(new MqttProperties.IntegerProperty(MqttProperties.MqttPropertyType.SUBSCRIPTION_IDENTIFIER.value(), outboundQos1Pub.getSubscriptionIdentifier()));
          }
          if (outboundQos1Pub.getTopicAlias() != null) {
            mqttProperties.add(new MqttProperties.IntegerProperty(MqttProperties.MqttPropertyType.TOPIC_ALIAS.value(), outboundQos1Pub.getTopicAlias()));
          }
          return mqttEndpoint.publish(outboundQos1Pub.getTopic(), io.vertx.mutiny.core.buffer.Buffer.newInstance(outboundQos1Pub.getPayload()),
              MqttQoS.valueOf(outboundQos1Pub.getQos()), true, outboundQos1Pub.isRetain(), outboundQos1Pub.getMessageId(), mqttProperties)
            .replaceWithVoid();
        }
      }).subscribe().with(ConsumerUtil.nothingToDo(), uniEmitter::fail, () -> uniEmitter.complete(null)));
  }

  private Uni<Void> resendOutboundQos2Pub(MqttEndpoint mqttEndpoint, String sessionId) {
    return Uni.createFrom().emitter(uniEmitter -> msgService.outboundQos2Pub(sessionId)
      .onItem().transformToMulti(outboundQos2Pubs -> Multi.createFrom().items(outboundQos2Pubs.stream()))
      .onItem().call(outboundQos2Pub -> {
        if (mqttEndpoint.protocolVersion() <= MqttVersion.MQTT_3_1_1.protocolLevel()) {
          return mqttEndpoint.publish(outboundQos2Pub.getTopic(), io.vertx.mutiny.core.buffer.Buffer.newInstance(outboundQos2Pub.getPayload()),
              MqttQoS.valueOf(outboundQos2Pub.getQos()), true, outboundQos2Pub.isRetain(), outboundQos2Pub.getMessageId())
            .replaceWithVoid();
        } else {
          MqttProperties mqttProperties = new MqttProperties();
          if (outboundQos2Pub.getMessageExpiryInterval() != null && outboundQos2Pub.getMessageExpiryInterval() != 0) {
            long messageExpiryInterval = outboundQos2Pub.getMessageExpiryInterval() - (Instant.now().toEpochMilli() - outboundQos2Pub.getCreatedTime()) / 1000;
            mqttProperties.add(new MqttProperties.IntegerProperty(MqttProperties.MqttPropertyType.PUBLICATION_EXPIRY_INTERVAL.value(), (int) messageExpiryInterval));
          }
          if (outboundQos2Pub.getPayloadFormatIndicator() != null) {
            mqttProperties.add(new MqttProperties.IntegerProperty(MqttProperties.MqttPropertyType.PAYLOAD_FORMAT_INDICATOR.value(), outboundQos2Pub.getPayloadFormatIndicator()));
          }
          if (StringUtils.isNotBlank(outboundQos2Pub.getContentType())) {
            mqttProperties.add(new MqttProperties.StringProperty(MqttProperties.MqttPropertyType.CONTENT_TYPE.value(), outboundQos2Pub.getContentType()));
          }
          if (StringUtils.isNotBlank(outboundQos2Pub.getResponseTopic())) {
            mqttProperties.add(new MqttProperties.StringProperty(MqttProperties.MqttPropertyType.RESPONSE_TOPIC.value(), outboundQos2Pub.getResponseTopic()));
          }
          if (outboundQos2Pub.getCorrelationData() != null) {
            mqttProperties.add(new MqttProperties.BinaryProperty(MqttProperties.MqttPropertyType.CORRELATION_DATA.value(), outboundQos2Pub.getCorrelationData().getBytes()));
          }
          if (outboundQos2Pub.getSubscriptionIdentifier() != null) {
            mqttProperties.add(new MqttProperties.IntegerProperty(MqttProperties.MqttPropertyType.SUBSCRIPTION_IDENTIFIER.value(), outboundQos2Pub.getSubscriptionIdentifier()));
          }
          if (outboundQos2Pub.getTopicAlias() != null) {
            mqttProperties.add(new MqttProperties.IntegerProperty(MqttProperties.MqttPropertyType.TOPIC_ALIAS.value(), outboundQos2Pub.getTopicAlias()));
          }
          return mqttEndpoint.publish(outboundQos2Pub.getTopic(), io.vertx.mutiny.core.buffer.Buffer.newInstance(outboundQos2Pub.getPayload()),
              MqttQoS.valueOf(outboundQos2Pub.getQos()), true, outboundQos2Pub.isRetain(), outboundQos2Pub.getMessageId(), mqttProperties)
            .replaceWithVoid();
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
