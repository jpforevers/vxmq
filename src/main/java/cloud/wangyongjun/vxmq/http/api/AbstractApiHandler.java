package cloud.wangyongjun.vxmq.http.api;

import cloud.wangyongjun.vxmq.http.AbstractHandler;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.core.Vertx;
import io.vertx.mutiny.ext.web.RoutingContext;

public abstract class AbstractApiHandler extends AbstractHandler {

  protected AbstractApiHandler(Vertx vertx) {
    super(vertx);
  }

  @Override
  public Uni<Void> handleRequest(RoutingContext routingContext) {
    return handleApiRequest(routingContext);
  }

  public abstract Uni<Void> handleApiRequest(RoutingContext routingContext);

}
