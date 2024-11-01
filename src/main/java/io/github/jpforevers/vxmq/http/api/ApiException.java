package io.github.jpforevers.vxmq.http.api;

import io.vertx.core.json.JsonObject;

public class ApiException extends RuntimeException {

  private final ApiErrorCode errorCode;
  private final String errorMessage;
  private final JsonObject errorExt;

  public ApiException(ApiErrorCode errorCode) {
    super(errorCode.name());
    this.errorCode = errorCode;
    this.errorMessage = errorCode.name();
    this.errorExt = new JsonObject();
  }

  public ApiException(ApiErrorCode errorCode, String errorMessage) {
    super(errorMessage);
    this.errorCode = errorCode;
    this.errorMessage = errorMessage;
    this.errorExt = new JsonObject();
  }

  public ApiException(ApiErrorCode errorCode, String errorMessage, JsonObject errorExt) {
    super(errorMessage);
    this.errorCode = errorCode;
    this.errorMessage = errorMessage;
    this.errorExt = errorExt;
  }

  public ApiErrorCode errorCode() {
    return errorCode;
  }

  public String errorMessage() {
    return errorMessage;
  }

  public JsonObject errorExt() {
    return errorExt;
  }

  public JsonObject toJson() {
    JsonObject result = new JsonObject();
    result.put("errorCode", errorCode);
    result.put("errorMessage", errorMessage);
    result.put("errorExt", errorExt);
    return result;
  }

}
