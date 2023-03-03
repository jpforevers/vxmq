package cloud.wangyongjun.vxmq.http.api.test;

import cloud.wangyongjun.vxmq.http.api.AbstractApiJsonResultHandler;
import cloud.wangyongjun.vxmq.http.api.ApiErrorCode;
import cloud.wangyongjun.vxmq.http.api.ApiException;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.core.Vertx;
import io.vertx.mutiny.ext.web.RoutingContext;

public class TestHandler extends AbstractApiJsonResultHandler {

  public TestHandler(Vertx vertx) {
    super(vertx);
  }

  @Override
  public Uni<Object> computeJsonResult(RoutingContext routingContext) {
//    int x = 1 / 0;
//    return Uni.createFrom().item(new JsonObject().put("x", "哈哈哈"));
    throw new ApiException(ApiErrorCode.FORBIDDEN, "没有权限");
  }

}
