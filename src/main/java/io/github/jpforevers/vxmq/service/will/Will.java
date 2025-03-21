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

package io.github.jpforevers.vxmq.service.will;

import io.github.jpforevers.vxmq.assist.ModelConstants;
import io.github.jpforevers.vxmq.assist.MqttPropertiesUtil;
import io.github.jpforevers.vxmq.assist.Nullable;
import io.netty.handler.codec.mqtt.MqttProperties;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class Will {

  private String sessionId;
  private String clientId;
  private String willTopicName;  // don't contain wildcard
  private Buffer willMessage;
  private int willQos;
  private boolean willRetain;
  // MqttProperties
  private Integer willDelayInterval;  // 3.1.3.2.2 Will Delay Interval, in seconds
  private Integer payloadFormatIndicator;  // 3.1.3.2.3 Payload Format Indicator
  private Integer messageExpiryInterval;  // 3.1.3.2.4 Message Expiry Interval
  private String contentType;  // 3.1.3.2.5 Content Type
  private String responseTopic;  // 3.1.3.2.6 Response Topic
  private Buffer correlationData;  // 3.1.3.2.7 Correlation Data
  private List<MqttProperties.StringPair> userProperties;  // 3.1.3.2.8 User Property

  private long createdTime;

  public Will() {
  }

  public Will(JsonObject jsonObject) {
    this.sessionId = jsonObject.getString(ModelConstants.FIELD_NAME_SESSION_ID);
    this.clientId = jsonObject.getString(ModelConstants.FIELD_NAME_CLIENT_ID);
    this.willTopicName = jsonObject.getString(ModelConstants.FIELD_NAME_WILL_TOPIC_NAME);
    this.willMessage = jsonObject.getBuffer(ModelConstants.FIELD_NAME_WILL_MESSAGE);
    this.willQos = jsonObject.getInteger(ModelConstants.FIELD_NAME_WILL_QOS);
    this.willRetain = jsonObject.getBoolean(ModelConstants.FIELD_NAME_WILL_RETAIN);
    this.willDelayInterval = jsonObject.getInteger(ModelConstants.FIELD_NAME_WILL_DELAY_INTERVAL);
    this.payloadFormatIndicator = jsonObject.getInteger(ModelConstants.FIELD_NAME_PAYLOAD_FORMAT_INDICATOR);
    this.messageExpiryInterval = jsonObject.getInteger(ModelConstants.FIELD_NAME_MESSAGE_EXPIRY_INTERVAL);
    this.contentType = jsonObject.getString(ModelConstants.FIELD_NAME_CONTENT_TYPE);
    this.responseTopic = jsonObject.getString(ModelConstants.FIELD_NAME_RESPONSE_TOPIC);
    this.correlationData = jsonObject.getBuffer(ModelConstants.FIELD_NAME_CORRELATION_DATA);
    this.userProperties = MqttPropertiesUtil.decodeUserProperties(jsonObject.getJsonArray(ModelConstants.FIELD_NAME_USER_PROPERTIES));
    this.createdTime = Instant.parse(jsonObject.getString(ModelConstants.FIELD_NAME_CREATED_TIME)).toEpochMilli();
  }

  public JsonObject toJson() {
    JsonObject jsonObject = new JsonObject();
    jsonObject.put(ModelConstants.FIELD_NAME_SESSION_ID, this.sessionId);
    jsonObject.put(ModelConstants.FIELD_NAME_CLIENT_ID, this.clientId);
    jsonObject.put(ModelConstants.FIELD_NAME_WILL_TOPIC_NAME, this.willTopicName);
    jsonObject.put(ModelConstants.FIELD_NAME_WILL_MESSAGE, this.willMessage);
    jsonObject.put(ModelConstants.FIELD_NAME_WILL_QOS, this.willQos);
    jsonObject.put(ModelConstants.FIELD_NAME_WILL_RETAIN, this.willRetain);
    jsonObject.put(ModelConstants.FIELD_NAME_WILL_DELAY_INTERVAL, this.willDelayInterval);
    jsonObject.put(ModelConstants.FIELD_NAME_PAYLOAD_FORMAT_INDICATOR, this.payloadFormatIndicator);
    jsonObject.put(ModelConstants.FIELD_NAME_MESSAGE_EXPIRY_INTERVAL, this.messageExpiryInterval);
    jsonObject.put(ModelConstants.FIELD_NAME_CONTENT_TYPE, this.contentType);
    jsonObject.put(ModelConstants.FIELD_NAME_RESPONSE_TOPIC, this.responseTopic);
    jsonObject.put(ModelConstants.FIELD_NAME_CORRELATION_DATA, this.correlationData);
    jsonObject.put(ModelConstants.FIELD_NAME_USER_PROPERTIES, MqttPropertiesUtil.encodeUserProperties(this.userProperties));
    jsonObject.put(ModelConstants.FIELD_NAME_CREATED_TIME, Instant.ofEpochMilli(this.createdTime).toString());
    return jsonObject;
  }

  @Override
  public String toString() {
    return toJson().toString();
  }

  public String getSessionId() {
    return sessionId;
  }

  public String getClientId() {
    return clientId;
  }

  public String getWillTopicName() {
    return willTopicName;
  }

  public Buffer getWillMessage() {
    return willMessage;
  }

  public int getWillQos() {
    return willQos;
  }

  public boolean isWillRetain() {
    return willRetain;
  }

  @Nullable
  public Integer getWillDelayInterval() {
    return willDelayInterval;
  }

  @Nullable
  public Integer getPayloadFormatIndicator() {
    return payloadFormatIndicator;
  }

  @Nullable
  public Integer getMessageExpiryInterval() {
    return messageExpiryInterval;
  }

  @Nullable
  public String getContentType() {
    return contentType;
  }

  @Nullable
  public String getResponseTopic() {
    return responseTopic;
  }

  @Nullable
  public Buffer getCorrelationData() {
    return correlationData;
  }

  @Nullable
  public List<MqttProperties.StringPair> getUserProperties() {
    return userProperties;
  }

  public long getCreatedTime() {
    return createdTime;
  }

  public Will setSessionId(String sessionId) {
    this.sessionId = sessionId;
    return this;
  }

  public Will setClientId(String clientId) {
    this.clientId = clientId;
    return this;
  }

  public Will setWillTopicName(String willTopicName) {
    this.willTopicName = willTopicName;
    return this;
  }

  public Will setWillMessage(Buffer willMessage) {
    this.willMessage = willMessage;
    return this;
  }

  public Will setWillQos(int willQos) {
    this.willQos = willQos;
    return this;
  }

  public Will setWillRetain(boolean willRetain) {
    this.willRetain = willRetain;
    return this;
  }

  public Will setWillDelayInterval(Integer willDelayInterval) {
    this.willDelayInterval = willDelayInterval;
    return this;
  }

  public Will setPayloadFormatIndicator(Integer payloadFormatIndicator) {
    this.payloadFormatIndicator = payloadFormatIndicator;
    return this;
  }

  public Will setMessageExpiryInterval(Integer messageExpiryInterval) {
    this.messageExpiryInterval = messageExpiryInterval;
    return this;
  }

  public Will setContentType(String contentType) {
    this.contentType = contentType;
    return this;
  }

  public Will setResponseTopic(String responseTopic) {
    this.responseTopic = responseTopic;
    return this;
  }

  public Will setCorrelationData(Buffer correlationData) {
    this.correlationData = correlationData;
    return this;
  }

  public Will setUserProperties(List<MqttProperties.StringPair> userProperties) {
    this.userProperties = userProperties;
    return this;
  }

  public Will addUserProperty(String key, String value) {
    if (this.userProperties == null) {
      userProperties = new ArrayList<>();
    }
    userProperties.add(new MqttProperties.StringPair(key, value));
    return this;
  }

  public Will setCreatedTime(long createdTime) {
    this.createdTime = createdTime;
    return this;
  }
}
