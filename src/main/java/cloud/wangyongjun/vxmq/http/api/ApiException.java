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
