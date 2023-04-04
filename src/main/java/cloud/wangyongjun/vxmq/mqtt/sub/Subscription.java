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

package cloud.wangyongjun.vxmq.mqtt.sub;

import cloud.wangyongjun.vxmq.assist.Nullable;
import cloud.wangyongjun.vxmq.mqtt.StringPair;
import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.time.Instant;
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
  private List<StringPair> userProperties;
  // MQTT5 Shared subscription
  private String shareName;

  private long createdTime;

  public Subscription() {
  }

  public Subscription(JsonObject jsonObject) {
    this.sessionId = jsonObject.getString("sessionId");
    this.clientId = jsonObject.getString("clientId");
    this.topicFilter = jsonObject.getString("topicFilter");
    this.qos = jsonObject.getInteger("qos");
    this.isNoLocal = jsonObject.getBoolean("isNoLocal");
    this.isRetainAsPublished = jsonObject.getBoolean("isRetainAsPublished");
    this.retainHandling = jsonObject.getInteger("retainHandling");
    this.subscriptionIdentifier = jsonObject.getInteger("subscriptionIdentifier");
    this.userProperties = jsonObject.getJsonArray("userProperties") == null ? new ArrayList<>() : jsonObject.getJsonArray("userProperties").stream().map(o -> (JsonObject) o).map(StringPair::new).collect(Collectors.toList());
    this.shareName = jsonObject.getString("shareName");
    this.createdTime = Instant.parse(jsonObject.getString("createdTime")).toEpochMilli();
  }

  public JsonObject toJson() {
    JsonObject jsonObject = new JsonObject();
    jsonObject.put("sessionId", this.sessionId);
    jsonObject.put("clientId", this.clientId);
    jsonObject.put("topicFilter", this.topicFilter);
    jsonObject.put("qos", this.qos);
    jsonObject.put("isNoLocal", this.isNoLocal);
    jsonObject.put("isRetainAsPublished", this.isRetainAsPublished);
    jsonObject.put("retainHandling", this.retainHandling);
    jsonObject.put("subscriptionIdentifier", this.subscriptionIdentifier);
    jsonObject.put("userProperties", this.userProperties == null ? new JsonArray() : this.userProperties.stream().map(StringPair::toJson).collect(JsonArray::new, JsonArray::add, JsonArray::addAll));
    jsonObject.put("shareName", this.shareName);
    jsonObject.put("createdTime", Instant.ofEpochMilli(this.createdTime).toString());
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

  public List<StringPair> getUserProperties() {
    return userProperties;
  }

  public Subscription setUserProperties(List<StringPair> userProperties) {
    this.userProperties = userProperties;
    return this;
  }

  public Subscription addUserProperty(String key, String value) {
    if (this.userProperties == null) {
      userProperties = new ArrayList<>();
    }
    userProperties.add(new StringPair(key, value));
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
