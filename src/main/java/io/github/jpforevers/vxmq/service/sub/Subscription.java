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

package io.github.jpforevers.vxmq.service.sub;

import io.github.jpforevers.vxmq.assist.ModelConstants;
import io.github.jpforevers.vxmq.assist.Nullable;
import io.netty.handler.codec.mqtt.MqttProperties;
import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@DataObject
public class Subscription {

  private String sessionId;
  private String clientId;
  private String topicFilter;
  private int qos;
  // MQTT5 Subscription options
  private Boolean isNoLocal;
  private Boolean isRetainAsPublished;
  private Integer retainHandling;
  // MQTT5 MqttProperties
  private Integer subscriptionIdentifier;
  private List<MqttProperties.StringPair> userProperties;
  // MQTT5 Shared subscription
  private String shareName;

  private long createdTime;

  public Subscription() {
  }

  public Subscription(JsonObject jsonObject) {
    this.sessionId = jsonObject.getString(ModelConstants.FIELD_NAME_SESSION_ID);
    this.clientId = jsonObject.getString(ModelConstants.FIELD_NAME_CLIENT_ID);
    this.topicFilter = jsonObject.getString(ModelConstants.FIELD_NAME_TOPIC_FILTER);
    this.qos = jsonObject.getInteger(ModelConstants.FIELD_NAME_QOS);
    this.isNoLocal = jsonObject.getBoolean(ModelConstants.FIELD_NAME_IS_NO_LOCAL);
    this.isRetainAsPublished = jsonObject.getBoolean(ModelConstants.FIELD_NAME_IS_RETAIN_AS_PUBLISHED);
    this.retainHandling = jsonObject.getInteger(ModelConstants.FIELD_NAME_RETAIN_HANDLING);
    this.subscriptionIdentifier = jsonObject.getInteger(ModelConstants.FIELD_NAME_SUBSCRIPTION_IDENTIFIER);
    this.userProperties = jsonObject.getJsonArray(ModelConstants.FIELD_NAME_USER_PROPERTIES) == null ? new ArrayList<>() :
      jsonObject.getJsonArray(ModelConstants.FIELD_NAME_USER_PROPERTIES).stream()
        .map(o -> (JsonObject) o)
        .map(j -> new MqttProperties.StringPair(j.getString("key"), j.getString("value")))
        .collect(Collectors.toList());
    this.shareName = jsonObject.getString(ModelConstants.FIELD_NAME_SHARE_NAME);
    this.createdTime = jsonObject.getLong(ModelConstants.FIELD_NAME_CREATED_TIME);
  }

  public JsonObject toJson() {
    JsonObject jsonObject = new JsonObject();
    jsonObject.put(ModelConstants.FIELD_NAME_SESSION_ID, this.sessionId);
    jsonObject.put(ModelConstants.FIELD_NAME_CLIENT_ID, this.clientId);
    jsonObject.put(ModelConstants.FIELD_NAME_TOPIC_FILTER, this.topicFilter);
    jsonObject.put(ModelConstants.FIELD_NAME_QOS, this.qos);
    jsonObject.put(ModelConstants.FIELD_NAME_IS_NO_LOCAL, this.isNoLocal);
    jsonObject.put(ModelConstants.FIELD_NAME_IS_RETAIN_AS_PUBLISHED, this.isRetainAsPublished);
    jsonObject.put(ModelConstants.FIELD_NAME_RETAIN_HANDLING, this.retainHandling);
    jsonObject.put(ModelConstants.FIELD_NAME_SUBSCRIPTION_IDENTIFIER, this.subscriptionIdentifier);
    jsonObject.put(ModelConstants.FIELD_NAME_USER_PROPERTIES, this.userProperties == null ? null :
      this.userProperties.stream()
        .map(stringPair -> new JsonObject().put("key", stringPair.key).put("value", stringPair.value))
        .collect(JsonArray::new, JsonArray::add, JsonArray::addAll));
    jsonObject.put(ModelConstants.FIELD_NAME_SHARE_NAME, this.shareName);
    jsonObject.put(ModelConstants.FIELD_NAME_CREATED_TIME, this.createdTime);
    return jsonObject;
  }

  public SubscriptionKey getKey() {
    return new SubscriptionKey(sessionId, topicFilter);
  }

  @Override
  public String toString() {
    return toJson().toString();
  }

  public String getSessionId() {
    return sessionId;
  }

  public Subscription setSessionId(String sessionId) {
    this.sessionId = sessionId;
    return this;
  }

  public String getClientId() {
    return clientId;
  }

  public Subscription setClientId(String clientId) {
    this.clientId = clientId;
    return this;
  }

  public String getTopicFilter() {
    return topicFilter;
  }

  public Subscription setTopicFilter(String topicFilter) {
    this.topicFilter = topicFilter;
    return this;
  }

  public int getQos() {
    return qos;
  }

  public Subscription setQos(int qos) {
    this.qos = qos;
    return this;
  }

  @Nullable
  public Boolean getNoLocal() {
    return isNoLocal;
  }

  public Subscription setNoLocal(Boolean noLocal) {
    isNoLocal = noLocal;
    return this;
  }

  @Nullable
  public Boolean getRetainAsPublished() {
    return isRetainAsPublished;
  }

  public Subscription setRetainAsPublished(Boolean retainAsPublished) {
    isRetainAsPublished = retainAsPublished;
    return this;
  }

  @Nullable
  public Integer getRetainHandling() {
    return retainHandling;
  }

  public Subscription setRetainHandling(Integer retainHandling) {
    this.retainHandling = retainHandling;
    return this;
  }

  @Nullable
  public Integer getSubscriptionIdentifier() {
    return subscriptionIdentifier;
  }

  public Subscription setSubscriptionIdentifier(Integer subscriptionIdentifier) {
    this.subscriptionIdentifier = subscriptionIdentifier;
    return this;
  }

  public List<MqttProperties.StringPair> getUserProperties() {
    return userProperties;
  }

  public Subscription setUserProperties(List<MqttProperties.StringPair> userProperties) {
    this.userProperties = userProperties;
    return this;
  }

  public Subscription addUserProperty(String key, String value) {
    if (this.userProperties == null) {
      userProperties = new ArrayList<>();
    }
    userProperties.add(new MqttProperties.StringPair(key, value));
    return this;
  }

  @Nullable
  public String getShareName() {
    return shareName;
  }

  public Subscription setShareName(String shareName) {
    this.shareName = shareName;
    return this;
  }

  public long getCreatedTime() {
    return createdTime;
  }

  public Subscription setCreatedTime(long createdTime) {
    this.createdTime = createdTime;
    return this;
  }

}
