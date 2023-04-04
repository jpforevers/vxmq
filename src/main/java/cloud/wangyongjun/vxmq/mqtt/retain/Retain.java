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

package cloud.wangyongjun.vxmq.mqtt.retain;

import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;

import java.time.Instant;

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
    this.topicName = jsonObject.getString("topicName");
    this.qos = jsonObject.getInteger("qos");
    this.payload = jsonObject.getBuffer("payload");
    this.createdTime = Instant.parse(jsonObject.getString("createdTime")).toEpochMilli();
  }

  public JsonObject toJson() {
    JsonObject jsonObject = new JsonObject();
    jsonObject.put("topicName", this.topicName);
    jsonObject.put("qos", this.qos);
    jsonObject.put("payload", this.payload);
    jsonObject.put("createdTime", Instant.ofEpochMilli(this.createdTime).toString());
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
