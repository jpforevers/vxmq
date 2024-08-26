package cloud.wangyongjun.vxmq.http.q.health;

import io.vertx.mutiny.core.Vertx;

public class StartedHealthCheckHandler extends HealthCheckHandler {

  public StartedHealthCheckHandler(Vertx vertx) {
    super(vertx);
  }

}
