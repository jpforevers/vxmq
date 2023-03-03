package cloud.wangyongjun.vxmq.assist;

import io.vertx.core.impl.VertxInternal;
import io.vertx.mutiny.core.Vertx;

public class VertxUtil {

  public static String getNodeId(Vertx vertx) {
    if (vertx.isClustered()){
      return ((VertxInternal) vertx.getDelegate()).getClusterManager().getNodeId();
    }else {
      throw new IllegalStateException("Can not get nodeId, because vertx is not clustered");
    }
  }

}
