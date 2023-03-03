package cloud.wangyongjun.vxmq.assist;

import cloud.wangyongjun.vxmq.mqtt.sub.mutiny.SubService;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.mutiny.core.Vertx;

public class VertxServiceProxyBuilder {

  public static SubService buildSubService(Vertx vertx) {
    io.vertx.serviceproxy.ServiceProxyBuilder builder = new io.vertx.serviceproxy.ServiceProxyBuilder(vertx.getDelegate())
      .setAddress(EBServices.SUB_SERVICE.getEbAddress())
      .setOptions(new DeliveryOptions().setLocalOnly(true));
    cloud.wangyongjun.vxmq.mqtt.sub.SubService subService = builder.build(cloud.wangyongjun.vxmq.mqtt.sub.SubService.class);
    return SubService.newInstance(subService);
  }

}
