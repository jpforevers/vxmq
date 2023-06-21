package cloud.wangyongjun.vxmq.http.api;

import io.netty.handler.codec.http.HttpResponseStatus;

public enum ApiErrorCode {

  COMMON_SUCCESS(HttpResponseStatus.OK.code()),
  COMMON_BAD_REQUEST(HttpResponseStatus.BAD_REQUEST.code()),
  COMMON_UNAUTHORIZED(HttpResponseStatus.UNAUTHORIZED.code()),
  COMMON_FORBIDDEN(HttpResponseStatus.FORBIDDEN.code()),
  COMMON_NOT_FOUND(HttpResponseStatus.NOT_FOUND.code()),
  COMMON_CONFLICT(HttpResponseStatus.CONFLICT.code()),
  COMMON_INTERNAL_SERVER_ERROR(HttpResponseStatus.INTERNAL_SERVER_ERROR.code()),
  ;

  private final int httpStatus;

  ApiErrorCode(int httpStatus) {
    this.httpStatus = httpStatus;
  }

  public int getHttpStatus() {
    return httpStatus;
  }

}
