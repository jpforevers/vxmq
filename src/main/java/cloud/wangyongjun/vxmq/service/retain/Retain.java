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

package cloud.wangyongjun.vxmq.service.retain;

import cloud.wangyongjun.vxmq.assist.ModelConstants;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;

public class Retain {

  private final String topicName;  // don't contain wildcard
  private final int qos;
  private final Buffer payload;
  private final long createdTime;

  public Retain(String topicName, int qos, Buffer payload, long createdTime) {
    this.topicName = topicName;
    this.qos = qos;
    this.payload = payload;
    this.createdTime = createdTime;
  }

  public Retain(JsonObject jsonObject) {
    this.topicName = jsonObject.getString(ModelConstants.FIELD_NAME_TOPIC_NAME);
    this.qos = jsonObject.getInteger(ModelConstants.FIELD_NAME_QOS);
    this.payload = jsonObject.getBuffer(ModelConstants.FIELD_NAME_PAYLOAD);
    this.createdTime = jsonObject.getLong(ModelConstants.FIELD_NAME_CREATED_TIME);
  }

  public JsonObject toJson() {
    JsonObject jsonObject = new JsonObject();
    jsonObject.put(ModelConstants.FIELD_NAME_TOPIC_NAME, this.topicName);
    jsonObject.put(ModelConstants.FIELD_NAME_QOS, this.qos);
    jsonObject.put(ModelConstants.FIELD_NAME_PAYLOAD, this.payload);
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

  public long getCreatedTime() {
    return createdTime;
  }

}
