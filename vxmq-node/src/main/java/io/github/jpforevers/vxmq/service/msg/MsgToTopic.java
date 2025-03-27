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

package io.github.jpforevers.vxmq.service.msg;

import io.github.jpforevers.vxmq.assist.ModelConstants;
import io.github.jpforevers.vxmq.assist.MqttPropertiesUtil;
import io.netty.handler.codec.mqtt.MqttProperties;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;

import java.util.ArrayList;
import java.util.List;

public class MsgToTopic {

  private String clientId;
  private String topic;
  private int qos;
  private Buffer payload;
  private boolean retain;
  private Integer messageExpiryInterval;
  private Integer payloadFormatIndicator;
  private String contentType;
  private String responseTopic;
  private Buffer correlationData;
  private Integer topicAlias;
  private List<MqttProperties.StringPair> userProperties;

  public MsgToTopic() {
  }

  public MsgToTopic(JsonObject jsonObject) {
    this.clientId = jsonObject.getString(ModelConstants.FIELD_NAME_CLIENT_ID);
    this.topic = jsonObject.getString(ModelConstants.FIELD_NAME_TOPIC);
    this.qos = jsonObject.getInteger(ModelConstants.FIELD_NAME_QOS);
    this.payload = jsonObject.getBuffer(ModelConstants.FIELD_NAME_PAYLOAD);
    this.retain = jsonObject.getBoolean(ModelConstants.FIELD_NAME_RETAIN);
    this.messageExpiryInterval = jsonObject.getInteger(ModelConstants.FIELD_NAME_MESSAGE_EXPIRY_INTERVAL);
    this.payloadFormatIndicator = jsonObject.getInteger(ModelConstants.FIELD_NAME_PAYLOAD_FORMAT_INDICATOR);
    this.contentType = jsonObject.getString(ModelConstants.FIELD_NAME_CONTENT_TYPE);
    this.responseTopic = jsonObject.getString(ModelConstants.FIELD_NAME_RESPONSE_TOPIC);
    this.correlationData = jsonObject.getBuffer(ModelConstants.FIELD_NAME_CORRELATION_DATA);
    this.topicAlias = jsonObject.getInteger(ModelConstants.FIELD_NAME_TOPIC_ALIAS);
    this.userProperties = MqttPropertiesUtil.decodeUserProperties(jsonObject.getJsonArray(ModelConstants.FIELD_NAME_USER_PROPERTIES));
  }

  public JsonObject toJson() {
    JsonObject jsonObject = new JsonObject();
    jsonObject.put(ModelConstants.FIELD_NAME_CLIENT_ID, this.clientId);
    jsonObject.put(ModelConstants.FIELD_NAME_TOPIC, this.topic);
    jsonObject.put(ModelConstants.FIELD_NAME_QOS, this.qos);
    jsonObject.put(ModelConstants.FIELD_NAME_PAYLOAD, this.payload);
    jsonObject.put(ModelConstants.FIELD_NAME_RETAIN, this.retain);
    jsonObject.put(ModelConstants.FIELD_NAME_MESSAGE_EXPIRY_INTERVAL, this.messageExpiryInterval);
    jsonObject.put(ModelConstants.FIELD_NAME_PAYLOAD_FORMAT_INDICATOR, this.payloadFormatIndicator);
    jsonObject.put(ModelConstants.FIELD_NAME_CONTENT_TYPE, this.contentType);
    jsonObject.put(ModelConstants.FIELD_NAME_RESPONSE_TOPIC, this.responseTopic);
    jsonObject.put(ModelConstants.FIELD_NAME_CORRELATION_DATA, this.correlationData);
    jsonObject.put(ModelConstants.FIELD_NAME_TOPIC_ALIAS, this.topicAlias);
    jsonObject.put(ModelConstants.FIELD_NAME_USER_PROPERTIES, MqttPropertiesUtil.encodeUserProperties(this.userProperties));
    return jsonObject;
  }

  @Override
  public String toString() {
    return toJson().toString();
  }

  public String getClientId() {
    return clientId;
  }

  public MsgToTopic setClientId(String clientId) {
    this.clientId = clientId;
    return this;
  }

  public String getTopic() {
    return topic;
  }

  public MsgToTopic setTopic(String topic) {
    this.topic = topic;
    return this;
  }

  public int getQos() {
    return qos;
  }

  public MsgToTopic setQos(int qos) {
    this.qos = qos;
    return this;
  }

  public Buffer getPayload() {
    return payload;
  }

  public MsgToTopic setPayload(Buffer payload) {
    this.payload = payload;
    return this;
  }

  public boolean isRetain() {
    return retain;
  }

  public MsgToTopic setRetain(boolean retain) {
    this.retain = retain;
    return this;
  }

  public Integer getMessageExpiryInterval() {
    return messageExpiryInterval;
  }

  public MsgToTopic setMessageExpiryInterval(Integer messageExpiryInterval) {
    this.messageExpiryInterval = messageExpiryInterval;
    return this;
  }

  public Integer getPayloadFormatIndicator() {
    return payloadFormatIndicator;
  }

  public MsgToTopic setPayloadFormatIndicator(Integer payloadFormatIndicator) {
    this.payloadFormatIndicator = payloadFormatIndicator;
    return this;
  }

  public String getContentType() {
    return contentType;
  }

  public MsgToTopic setContentType(String contentType) {
    this.contentType = contentType;
    return this;
  }

  public String getResponseTopic() {
    return responseTopic;
  }

  public MsgToTopic setResponseTopic(String responseTopic) {
    this.responseTopic = responseTopic;
    return this;
  }

  public Buffer getCorrelationData() {
    return correlationData;
  }

  public MsgToTopic setCorrelationData(Buffer correlationData) {
    this.correlationData = correlationData;
    return this;
  }

  public Integer getTopicAlias() {
    return topicAlias;
  }

  public MsgToTopic setTopicAlias(Integer topicAlias) {
    this.topicAlias = topicAlias;
    return this;
  }

  public List<MqttProperties.StringPair> getUserProperties() {
    return userProperties;
  }

  public MsgToTopic setUserProperties(List<MqttProperties.StringPair> userProperties) {
    this.userProperties = userProperties;
    return this;
  }

  public MsgToTopic addUserProperties(MqttProperties.StringPair userProperty) {
    if (this.userProperties == null) {
      this.userProperties = new ArrayList<>();
    }
    this.userProperties.add(userProperty);
    return this;
  }
}
