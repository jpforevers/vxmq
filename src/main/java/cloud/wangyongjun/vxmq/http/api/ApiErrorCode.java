package cloud.wangyongjun.vxmq.http.api;

public enum ApiErrorCode {

  INTERNAL_SERVER_ERROR(500),
  FORBIDDEN(403);

  private final int httpStatus;

  ApiErrorCode(int httpStatus) {
    this.httpStatus = httpStatus;
  }

  public int getHttpStatus() {
    return httpStatus;
  }
}
