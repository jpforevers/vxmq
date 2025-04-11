package io.github.jpforevers.vxmq.http.api.v2;

import io.github.jpforevers.vxmq.http.api.AbstractApiHandler;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.core.Vertx;
import io.vertx.mutiny.ext.web.RoutingContext;

public class AuthHandler extends AbstractApiHandler{

  public AuthHandler(Vertx vertx) {
    super(vertx);
  }  

  @Override
  public Uni<Void> handleApiRequest(RoutingContext routingContext) {

    return doAuth()
      .onItem().invoke(() -> routingContext.next());
  }

  public Uni<Void> doAuth() {
    // int x = 1/0;
    return Uni.createFrom().voidItem();
  }

}
