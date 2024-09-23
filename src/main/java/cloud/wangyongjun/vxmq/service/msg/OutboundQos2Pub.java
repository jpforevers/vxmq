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

import java.util.List;

public class OutboundQos2Pub {

  private String sessionId;
  private String clientId;
  private int messageId;
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
  private List<MqttProperties.StringPair> userProperties;
  private long createdTime;

  public OutboundQos2Pub() {
  }

  public OutboundQos2Pub(String sessionId, String clientId, int messageId, String topic, int qos,
                         Buffer payload, boolean dup, boolean retain,
                         Integer messageExpiryInterval, Integer payloadFormatIndicator, String contentType,
                         String responseTopic, Buffer correlationData, Integer subscriptionIdentifier,
                         List<MqttProperties.StringPair> userProperties, long createdTime) {
    this.sessionId = sessionId;
    this.clientId = clientId;
    this.messageId = messageId;
    this.topic = topic;
    this.qos = qos;
    this.payload = payload;
    this.dup = dup;
    this.retain = retain;
    this.messageExpiryInterval = messageExpiryInterval;
    this.payloadFormatIndicator = payloadFormatIndicator;
    this.contentType = contentType;
    this.responseTopic = responseTopic;
    this.correlationData = correlationData;
    this.subscriptionIdentifier = subscriptionIdentifier;
    this.userProperties = userProperties;
    this.createdTime = createdTime;
  }

  public OutboundQos2Pub(JsonObject jsonObject) {
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

  public String getClientId() {
    return clientId;
  }

  public int getMessageId() {
    return messageId;
  }

  public String getTopic() {
    return topic;
  }

  public int getQos() {
    return qos;
  }

  public Buffer getPayload() {
    return payload;
  }

  public boolean isDup() {
    return dup;
  }

  public boolean isRetain() {
    return retain;
  }

  public Integer getMessageExpiryInterval() {
    return messageExpiryInterval;
  }

  public Integer getPayloadFormatIndicator() {
    return payloadFormatIndicator;
  }

  public String getContentType() {
    return contentType;
  }

  public String getResponseTopic() {
    return responseTopic;
  }

  public Buffer getCorrelationData() {
    return correlationData;
  }

  public Integer getSubscriptionIdentifier() {
    return subscriptionIdentifier;
  }

  public List<MqttProperties.StringPair> getUserProperties() {
    return userProperties;
  }

  public long getCreatedTime() {
    return createdTime;
  }

  public OutboundQos2PubKey getKey() {
    return new OutboundQos2PubKey(sessionId, messageId);
  }

}
