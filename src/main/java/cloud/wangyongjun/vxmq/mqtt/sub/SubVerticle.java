package cloud.wangyongjun.vxmq.mqtt.sub;

import cloud.wangyongjun.vxmq.assist.EBServices;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.vertx.core.AbstractVerticle;
import io.vertx.serviceproxy.ServiceBinder;

// 部署Verticle时，如果实例数量为1，那么该Verticle是完全线程安全的，始终只有一个并发线程运行该Verticle。
// 我们利用该机制来实现订阅树的线程安全。
public class SubVerticle extends AbstractVerticle {

  @Override
  public Uni<Void> asyncStart() {
    SubService subService = IgniteAndSubTreeSubService.getInstance(vertx, config());
    new ServiceBinder(vertx.getDelegate()).setAddress(EBServices.SUB_SERVICE.getEbAddress())
      .registerLocal(SubService.class, subService);
    return Uni.createFrom().voidItem();
  }

  @Override
  public Uni<Void> asyncStop() {
    return Uni.createFrom().voidItem();
  }

}
