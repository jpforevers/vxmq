package io.github.jpforevers.vxmq.service.sub.share;

import io.github.jpforevers.vxmq.service.sub.Subscription;
import io.vertx.mutiny.core.Vertx;

import java.util.List;

public interface ShareSubscriptionsLoadBalancer {

  static ShareSubscriptionsLoadBalancer create(Vertx vertx){
    return ShareSubscriptionsRoundRobinLoadBalancer.getInstance(vertx);
  }

  /**
   * Load balance from same share name subscriptions
   * @param subscriptions same share name subscriptions
   * @return Load balance result
   */
  Subscription next(List<Subscription> subscriptions);

}
