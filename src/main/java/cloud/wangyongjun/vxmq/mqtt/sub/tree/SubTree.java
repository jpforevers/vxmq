package cloud.wangyongjun.vxmq.mqtt.sub.tree;

import cloud.wangyongjun.vxmq.mqtt.sub.Subscription;
import cloud.wangyongjun.vxmq.mqtt.sub.tree.impl.SubTreeTrieAndRecursiveImpl;

import java.util.*;
import java.util.stream.Collectors;

public interface SubTree {

  static SubTree subTree() {
    return new SubTreeTrieAndRecursiveImpl();
  }

  void saveOrUpdateSubscription(Subscription subscription);

  List<Subscription> findAllMatch(String topicName, boolean distinct);

  List<Subscription> allSubs();

  void removeSubscription(String sessionId, String topicFilter);

  void clearSubscription(String sessionId);

  static List<Subscription> distinct(List<Subscription> subscriptions) {
    Map<String, List<Subscription>> sessionIdToSubsMap = subscriptions.stream().collect(Collectors.groupingBy(Subscription::getSessionId));
    Map<String, Subscription> sessionIdToSubMap = new HashMap<>();
    sessionIdToSubsMap.forEach((sessionId, subs) -> {
      if (subs.size() > 1) {
        sessionIdToSubMap.put(sessionId, subs.stream().max(Comparator.comparing(Subscription::getQos)).get());
      } else {
        sessionIdToSubMap.put(sessionId, subs.get(0));
      }
    });
    return new ArrayList<>(sessionIdToSubMap.values());
  }

}
