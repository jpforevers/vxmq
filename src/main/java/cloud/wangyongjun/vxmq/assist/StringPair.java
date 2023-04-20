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

package cloud.wangyongjun.vxmq.assist;

import io.vertx.core.json.JsonObject;

public class StringPair {

  private String key;
  private String value;

  public StringPair() {
  }

  public StringPair(String key, String value) {
    this.key = key;
    this.value = value;
  }

  public StringPair(JsonObject jsonObject) {
    this.key = jsonObject.getString("key");
    this.value = jsonObject.getString("value");
  }

  public JsonObject toJson() {
    JsonObject jsonObject = new JsonObject();
    jsonObject.put("key", key);
    jsonObject.put("value", value);
    return jsonObject;
  }

  public String getKey() {
    return key;
  }

  public String getValue() {
    return value;
  }
}
