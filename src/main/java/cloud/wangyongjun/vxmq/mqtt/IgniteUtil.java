package cloud.wangyongjun.vxmq.mqtt;

import io.vertx.core.impl.VertxInternal;
import io.vertx.mutiny.core.Vertx;
import io.vertx.spi.cluster.ignite.IgniteClusterManager;
import org.apache.ignite.Ignite;

public class IgniteUtil {

  public static Ignite getIgnite(Vertx vertx) {
    return ((IgniteClusterManager) (((VertxInternal) vertx.getDelegate()).getClusterManager())).getIgniteInstance();
  }

}
