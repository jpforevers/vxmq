package cloud.wangyongjun.vxmq.service.sub.share;

import cloud.wangyongjun.vxmq.service.sub.Subscription;
import io.vertx.mutiny.core.Vertx;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ShareSubscriptionProcessor {

  private static volatile ShareSubscriptionProcessor shareSubscriptionProcessor;

  public static ShareSubscriptionProcessor getInstance(Vertx vertx) {
    if (shareSubscriptionProcessor == null) {
      synchronized (ShareSubscriptionProcessor.class) {
        if (shareSubscriptionProcessor == null) {
          shareSubscriptionProcessor = new ShareSubscriptionProcessor(vertx, ShareSubscriptionsLoadBalancer.create(vertx));
        }
      }
    }
    return shareSubscriptionProcessor;
  }

  private final Vertx vertx;
  private final ShareSubscriptionsLoadBalancer shareSubscriptionsLoadBalancer;

  public ShareSubscriptionProcessor(Vertx vertx, ShareSubscriptionsLoadBalancer shareSubscriptionsLoadBalancer) {
    this.vertx = vertx;
    this.shareSubscriptionsLoadBalancer = shareSubscriptionsLoadBalancer;
  }

  public List<Subscription> process(List<Subscription> allMatchSubscriptions) {
    List<Subscription> allMatchSubscriptionsShared = allMatchSubscriptions.stream().filter(subscription -> StringUtils.isNotBlank(subscription.getShareName())).toList();
    if (allMatchSubscriptionsShared.isEmpty()) {
      return allMatchSubscriptions;
    } else {
      List<Subscription> allMatchSubscriptionsNonShared = allMatchSubscriptions.stream().filter(subscription -> StringUtils.isBlank(subscription.getShareName())).toList();
      List<Subscription> result = new ArrayList<>(allMatchSubscriptionsNonShared);

      Map<String, List<Subscription>> shareNameToSubscriptionsMap = allMatchSubscriptionsShared.stream().collect(Collectors.groupingBy(Subscription::getShareName));
      for (Map.Entry<String, List<Subscription>> shareNameToSubscriptionsEntry : shareNameToSubscriptionsMap.entrySet()) {
        List<Subscription> shareNameSubscriptions = shareNameToSubscriptionsEntry.getValue();
        if (shareNameSubscriptions.size() == 1) {
          result.addAll(shareNameSubscriptions);
        } else {
          Map<String, List<Subscription>> topicFilterToSubscriptionsMap = shareNameSubscriptions.stream().collect(Collectors.groupingBy(Subscription::getTopicFilter));
          for (Map.Entry<String, List<Subscription>> topicFilterToSubscriptionsEntry : topicFilterToSubscriptionsMap.entrySet()) {
            List<Subscription> topicFilterSubscriptions = topicFilterToSubscriptionsEntry.getValue();
            if (topicFilterSubscriptions.size() == 1) {
              result.addAll(topicFilterSubscriptions);
            } else {
              result.add(shareSubscriptionsLoadBalancer.next(topicFilterSubscriptions));
            }
          }
        }
      }
      return result;
    }
  }

}
