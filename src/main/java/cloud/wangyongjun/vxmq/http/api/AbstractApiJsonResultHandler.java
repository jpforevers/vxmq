package cloud.wangyongjun.vxmq.http.api;

import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.core.Vertx;
import io.vertx.mutiny.ext.web.RoutingContext;

public abstract class AbstractApiJsonResultHandler extends AbstractApiHandler {

  protected AbstractApiJsonResultHandler(Vertx vertx) {
    super(vertx);
  }

  @Override
  public Uni<Void> handleApiRequest(RoutingContext routingContext) {
    return computeJsonResult(routingContext)
      .onItem().transformToUni(routingContext::json);
  }

  public abstract Uni<Object> computeJsonResult(RoutingContext routingContext);

}
