package cloud.wangyongjun.vxmq;

import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import io.vertx.mutiny.core.Vertx;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(VertxExtension.class)
public class BaseTest {

  private static VxmqLauncher vxmqLauncher;

  @BeforeAll
  static void startServer(Vertx vertx, VertxTestContext testContext) throws Throwable {
    if (vxmqLauncher == null) {
      vxmqLauncher = new VxmqLauncher();
      vxmqLauncher.start().subscribe().with(v -> testContext.completeNow(), testContext::failNow);
    } else {
      testContext.completeNow();
    }
  }

}
