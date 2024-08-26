package cloud.wangyongjun.vxmq.http.q.health;

import io.vertx.mutiny.core.Vertx;

public class ReadyHealthCheckHandler extends HealthCheckHandler {

  public ReadyHealthCheckHandler(Vertx vertx) {
    super(vertx);
  }

}
