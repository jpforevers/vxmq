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

package cloud.wangyongjun.vxmq.http.api;

import io.vertx.core.json.JsonObject;

public class ApiException extends RuntimeException {

  public final int httpStatus;
  public final String errorCode;
  public String errorMessage;

  public ApiException(ApiErrorCode apiErrorCode) {
    this.httpStatus = apiErrorCode.getHttpStatus();
    this.errorCode = apiErrorCode.name();
  }

  public ApiException(ApiErrorCode apiErrorCode, String errorMessage) {
    this.httpStatus = apiErrorCode.getHttpStatus();
    this.errorCode = apiErrorCode.name();
    this.errorMessage = errorMessage;
  }

  public JsonObject toJson() {
    JsonObject jsonObject = new JsonObject();
    jsonObject.put("errorCode", this.errorCode);
    jsonObject.put("errorMessage", this.errorMessage);
    return jsonObject;
  }

}
