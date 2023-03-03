package cloud.wangyongjun.vxmq.http.api;

import io.vertx.mutiny.ext.web.RoutingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Consumer;

public class ApiFailureHandler implements Consumer<RoutingContext> {

  private static final Logger LOGGER = LoggerFactory.getLogger(ApiFailureHandler.class);

  @Override
  public void accept(RoutingContext routingContext) {
    Throwable throwable = routingContext.failure();
    LOGGER.error("Error occurred when handling api request", throwable);
    if (throwable instanceof ApiException apiException) {
      routingContext.response().setStatusCode(apiException.httpStatus);
      routingContext.json(apiException.toJson()).subscribe().with(v -> {
      }, t -> {
      });
    } else {
      routingContext.response().setStatusCode(500);
      routingContext.json(new ApiException(ApiErrorCode.INTERNAL_SERVER_ERROR, throwable.getMessage()).toJson()).subscribe().with(v -> {
      }, t -> {
      });
    }
  }

}
