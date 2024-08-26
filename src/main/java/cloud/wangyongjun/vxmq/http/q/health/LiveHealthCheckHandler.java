package cloud.wangyongjun.vxmq.http.q.health;

import io.vertx.mutiny.core.Vertx;

public class LiveHealthCheckHandler extends HealthCheckHandler {

  public LiveHealthCheckHandler(Vertx vertx) {
    super(vertx);
  }

}
