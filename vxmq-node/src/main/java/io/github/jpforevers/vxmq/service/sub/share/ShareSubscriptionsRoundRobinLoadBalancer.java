package io.github.jpforevers.vxmq.service.sub.share;

import io.github.jpforevers.vxmq.service.sub.Subscription;
import io.vertx.mutiny.core.Vertx;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

class ShareSubscriptionsRoundRobinLoadBalancer implements ShareSubscriptionsLoadBalancer {

  private static volatile ShareSubscriptionsRoundRobinLoadBalancer loadBalancer;

  public static ShareSubscriptionsRoundRobinLoadBalancer getInstance(Vertx vertx) {
    if (loadBalancer == null) {
      synchronized (ShareSubscriptionsRoundRobinLoadBalancer.class) {
        if (loadBalancer == null) {
          loadBalancer = new ShareSubscriptionsRoundRobinLoadBalancer(vertx);
        }
      }
    }
    return loadBalancer;
  }

  private final Vertx vertx;
  private final Map<String, Integer> shareNameToCurrentIndexMap = new ConcurrentHashMap<>();

  private ShareSubscriptionsRoundRobinLoadBalancer(Vertx vertx) {
    this.vertx = vertx;
  }

  public Subscription next(List<Subscription> subscriptions) {
    int index = shareNameToCurrentIndexMap.compute(subscriptions.get(0).getShareName(), (k,v) -> {
      if (v == null) {
        v = -1;
      }
      v = (v + 1) % subscriptions.size();
      return v;
    });
    return subscriptions.get(index);
  }

  public static void main(String[] args) {
    List<Subscription> subscriptions = List.of(
      new Subscription().setSessionId("s1").setClientId("c1").setTopicFilter("abc/def/123").setQos(1).setShareName("g1").setCreatedTime(Instant.now().toEpochMilli()),
      new Subscription().setSessionId("s2").setClientId("c2").setTopicFilter("abc/def/123").setQos(1).setShareName("g1").setCreatedTime(Instant.now().toEpochMilli()),
      new Subscription().setSessionId("s3").setClientId("c3").setTopicFilter("abc/def/123").setQos(1).setShareName("g1").setCreatedTime(Instant.now().toEpochMilli())
    );
    for (int i = 0; i < 9; i++) {
      Subscription subscription = ShareSubscriptionsRoundRobinLoadBalancer.getInstance(Vertx.vertx()).next(subscriptions);
      System.out.println(subscription);
    }
  }

}
