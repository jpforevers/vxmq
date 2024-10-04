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

package cloud.wangyongjun.vxmq.service.msg;

import cloud.wangyongjun.vxmq.assist.ModelConstants;
import cloud.wangyongjun.vxmq.assist.MqttPropertiesUtil;
import io.netty.handler.codec.mqtt.MqttProperties;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;

import java.util.ArrayList;
import java.util.List;

public class MsgToClient {

  private String sessionId;
  private String clientId;
  private Integer messageId;
  private String topic;
  private int qos;
  private Buffer payload;
  private boolean dup;
  private boolean retain;
  private Integer messageExpiryInterval;
  private Integer payloadFormatIndicator;
  private String contentType;
  private String responseTopic;
  private Buffer correlationData;
  private Integer subscriptionIdentifier;
  private Integer topicAlias;
  private List<MqttProperties.StringPair> userProperties;
  private long createdTime;

  public MsgToClient() {
  }

  public MsgToClient(JsonObject jsonObject) {
    this.sessionId = jsonObject.getString(ModelConstants.FIELD_NAME_SESSION_ID);
    this.clientId = jsonObject.getString(ModelConstants.FIELD_NAME_CLIENT_ID);
    this.messageId = jsonObject.getInteger(ModelConstants.FIELD_NAME_MESSAGE_ID);
    this.topic = jsonObject.getString(ModelConstants.FIELD_NAME_TOPIC);
    this.qos = jsonObject.getInteger(ModelConstants.FIELD_NAME_QOS);
    this.payload = jsonObject.getBuffer(ModelConstants.FIELD_NAME_PAYLOAD);
    this.dup = jsonObject.getBoolean(ModelConstants.FIELD_NAME_DUP);
    this.retain = jsonObject.getBoolean(ModelConstants.FIELD_NAME_RETAIN);
    this.messageExpiryInterval = jsonObject.getInteger(ModelConstants.FIELD_NAME_MESSAGE_EXPIRY_INTERVAL);
    this.payloadFormatIndicator = jsonObject.getInteger(ModelConstants.FIELD_NAME_PAYLOAD_FORMAT_INDICATOR);
    this.contentType = jsonObject.getString(ModelConstants.FIELD_NAME_CONTENT_TYPE);
    this.responseTopic = jsonObject.getString(ModelConstants.FIELD_NAME_RESPONSE_TOPIC);
    this.correlationData = jsonObject.getBuffer(ModelConstants.FIELD_NAME_CORRELATION_DATA);
    this.subscriptionIdentifier = jsonObject.getInteger(ModelConstants.FIELD_NAME_SUBSCRIPTION_IDENTIFIER);
    this.topicAlias = jsonObject.getInteger(ModelConstants.FIELD_NAME_TOPIC_ALIAS);
    this.userProperties = MqttPropertiesUtil.decodeUserProperties(jsonObject.getJsonArray(ModelConstants.FIELD_NAME_USER_PROPERTIES));
    this.createdTime = jsonObject.getLong(ModelConstants.FIELD_NAME_CREATED_TIME);
  }

  public JsonObject toJson() {
    JsonObject jsonObject = new JsonObject();
    jsonObject.put(ModelConstants.FIELD_NAME_SESSION_ID, this.sessionId);
    jsonObject.put(ModelConstants.FIELD_NAME_CLIENT_ID, this.clientId);
    jsonObject.put(ModelConstants.FIELD_NAME_MESSAGE_ID, this.messageId);
    jsonObject.put(ModelConstants.FIELD_NAME_TOPIC, this.topic);
    jsonObject.put(ModelConstants.FIELD_NAME_QOS, this.qos);
    jsonObject.put(ModelConstants.FIELD_NAME_PAYLOAD, this.payload);
    jsonObject.put(ModelConstants.FIELD_NAME_DUP, this.dup);
    jsonObject.put(ModelConstants.FIELD_NAME_RETAIN, this.retain);
    jsonObject.put(ModelConstants.FIELD_NAME_MESSAGE_EXPIRY_INTERVAL, this.messageExpiryInterval);
    jsonObject.put(ModelConstants.FIELD_NAME_PAYLOAD_FORMAT_INDICATOR, this.payloadFormatIndicator);
    jsonObject.put(ModelConstants.FIELD_NAME_CONTENT_TYPE, this.contentType);
    jsonObject.put(ModelConstants.FIELD_NAME_RESPONSE_TOPIC, this.responseTopic);
    jsonObject.put(ModelConstants.FIELD_NAME_CORRELATION_DATA, this.correlationData);
    jsonObject.put(ModelConstants.FIELD_NAME_SUBSCRIPTION_IDENTIFIER, this.subscriptionIdentifier);
    jsonObject.put(ModelConstants.FIELD_NAME_TOPIC_ALIAS, this.topicAlias);
    jsonObject.put(ModelConstants.FIELD_NAME_USER_PROPERTIES, MqttPropertiesUtil.encodeUserProperties(this.userProperties));
    jsonObject.put(ModelConstants.FIELD_NAME_CREATED_TIME, this.createdTime);
    return jsonObject;
  }

  @Override
  public String toString() {
    return toJson().toString();
  }

  public String getSessionId() {
    return sessionId;
  }

  public MsgToClient setSessionId(String sessionId) {
    this.sessionId = sessionId;
    return this;
  }

  public String getClientId() {
    return clientId;
  }

  public MsgToClient setClientId(String clientId) {
    this.clientId = clientId;
    return this;
  }

  public Integer getMessageId() {
    return messageId;
  }

  public MsgToClient setMessageId(Integer messageId) {
    this.messageId = messageId;
    return this;
  }

  public String getTopic() {
    return topic;
  }

  public MsgToClient setTopic(String topic) {
    this.topic = topic;
    return this;
  }

  public int getQos() {
    return qos;
  }

  public MsgToClient setQos(int qos) {
    this.qos = qos;
    return this;
  }

  public Buffer getPayload() {
    return payload;
  }

  public MsgToClient setPayload(Buffer payload) {
    this.payload = payload;
    return this;
  }

  public boolean isDup() {
    return dup;
  }

  public MsgToClient setDup(boolean dup) {
    this.dup = dup;
    return this;
  }

  public boolean isRetain() {
    return retain;
  }

  public MsgToClient setRetain(boolean retain) {
    this.retain = retain;
    return this;
  }

  public Integer getMessageExpiryInterval() {
    return messageExpiryInterval;
  }

  public MsgToClient setMessageExpiryInterval(Integer messageExpiryInterval) {
    this.messageExpiryInterval = messageExpiryInterval;
    return this;
  }

  public Integer getPayloadFormatIndicator() {
    return payloadFormatIndicator;
  }

  public MsgToClient setPayloadFormatIndicator(Integer payloadFormatIndicator) {
    this.payloadFormatIndicator = payloadFormatIndicator;
    return this;
  }

  public String getContentType() {
    return contentType;
  }

  public MsgToClient setContentType(String contentType) {
    this.contentType = contentType;
    return this;
  }

  public String getResponseTopic() {
    return responseTopic;
  }

  public MsgToClient setResponseTopic(String responseTopic) {
    this.responseTopic = responseTopic;
    return this;
  }

  public Buffer getCorrelationData() {
    return correlationData;
  }

  public MsgToClient setCorrelationData(Buffer correlationData) {
    this.correlationData = correlationData;
    return this;
  }

  public Integer getSubscriptionIdentifier() {
    return subscriptionIdentifier;
  }

  public MsgToClient setSubscriptionIdentifier(Integer subscriptionIdentifier) {
    this.subscriptionIdentifier = subscriptionIdentifier;
    return this;
  }

  public Integer getTopicAlias() {
    return topicAlias;
  }

  public MsgToClient setTopicAlias(Integer topicAlias) {
    this.topicAlias = topicAlias;
    return this;
  }

  public List<MqttProperties.StringPair> getUserProperties() {
    return userProperties;
  }

  public MsgToClient setUserProperties(List<MqttProperties.StringPair> userProperties) {
    this.userProperties = userProperties;
    return this;
  }

  public MsgToClient addUserProperties(MqttProperties.StringPair userProperty) {
    if (this.userProperties == null) {
      this.userProperties = new ArrayList<>();
    }
    this.userProperties.add(userProperty);
    return this;
  }

  public long getCreatedTime() {
    return createdTime;
  }

  public MsgToClient setCreatedTime(long createdTime) {
    this.createdTime = createdTime;
    return this;
  }
}
