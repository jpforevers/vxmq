package cloud.wangyongjun.vxmq.http;

import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.core.Vertx;
import io.vertx.mutiny.ext.web.RoutingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Consumer;

public abstract class AbstractHandler implements Consumer<RoutingContext> {

  protected final Logger logger = LoggerFactory.getLogger(this.getClass());

  protected final Vertx vertx;

  protected AbstractHandler(Vertx vertx) {
    this.vertx = vertx;
  }

  @Override
  public void accept(RoutingContext routingContext) {
    logger.debug("Request from {} to {}", routingContext.request().remoteAddress(), routingContext.request().uri());
    handleRequest(routingContext).subscribe().with(v -> {
    }, routingContext::fail);
  }

  public abstract Uni<Void> handleRequest(RoutingContext routingContext);

}
