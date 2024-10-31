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

package io.github.jpforevers.vxmq.service.retain;

import io.github.jpforevers.vxmq.assist.ModelConstants;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;

public class Retain {

  private final String topicName;  // don't contain wildcard
  private final int qos;
  private final Buffer payload;
  private final Integer payloadFormatIndicator;
  private final String contentType;
  private final long createdTime;

  public Retain(String topicName, int qos, Buffer payload, Integer payloadFormatIndicator, String contentType, long createdTime) {
    this.topicName = topicName;
    this.qos = qos;
    this.payload = payload;
    this.payloadFormatIndicator = payloadFormatIndicator;
    this.contentType = contentType;
    this.createdTime = createdTime;
  }

  public Retain(JsonObject jsonObject) {
    this.topicName = jsonObject.getString(ModelConstants.FIELD_NAME_TOPIC_NAME);
    this.qos = jsonObject.getInteger(ModelConstants.FIELD_NAME_QOS);
    this.payload = jsonObject.getBuffer(ModelConstants.FIELD_NAME_PAYLOAD);
    this.payloadFormatIndicator = jsonObject.getInteger(ModelConstants.FIELD_NAME_PAYLOAD_FORMAT_INDICATOR);
    this.contentType = jsonObject.getString(ModelConstants.FIELD_NAME_CONTENT_TYPE);
    this.createdTime = jsonObject.getLong(ModelConstants.FIELD_NAME_CREATED_TIME);
  }

  public JsonObject toJson() {
    JsonObject jsonObject = new JsonObject();
    jsonObject.put(ModelConstants.FIELD_NAME_TOPIC_NAME, this.topicName);
    jsonObject.put(ModelConstants.FIELD_NAME_QOS, this.qos);
    jsonObject.put(ModelConstants.FIELD_NAME_PAYLOAD, this.payload);
    jsonObject.put(ModelConstants.FIELD_NAME_PAYLOAD_FORMAT_INDICATOR, this.payloadFormatIndicator);
    jsonObject.put(ModelConstants.FIELD_NAME_CONTENT_TYPE, this.contentType);
    jsonObject.put(ModelConstants.FIELD_NAME_CREATED_TIME, this.createdTime);
    return jsonObject;
  }

  @Override
  public String toString() {
    return toJson().toString();
  }

  public String getTopicName() {
    return topicName;
  }

  public int getQos() {
    return qos;
  }

  public Buffer getPayload() {
    return payload;
  }

  public Integer getPayloadFormatIndicator() {
    return payloadFormatIndicator;
  }

  public String getContentType() {
    return contentType;
  }

  public long getCreatedTime() {
    return createdTime;
  }

}
