package cloud.wangyongjun.vxmq.mqtt.sub;

import cloud.wangyongjun.vxmq.assist.ConsumerUtil;
import cloud.wangyongjun.vxmq.mqtt.IgniteAssist;
import cloud.wangyongjun.vxmq.mqtt.IgniteUtil;
import cloud.wangyongjun.vxmq.mqtt.TopicUtil;
import cloud.wangyongjun.vxmq.mqtt.sub.tree.SubTree;
import io.smallrye.mutiny.Uni;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.mutiny.core.Vertx;
import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteCache;
import org.apache.ignite.cache.query.ContinuousQuery;
import org.apache.ignite.cache.query.QueryCursor;
import org.apache.ignite.cache.query.ScanQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.cache.Cache;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

/**
 * Singleton. SubTree is not thread safe.
 */
public class IgniteAndSubTreeSubService implements SubService {

  private final static Logger LOGGER = LoggerFactory.getLogger(IgniteAndSubTreeSubService.class);

  private static volatile IgniteAndSubTreeSubService igniteAndSubTreeSubService;

  public static IgniteAndSubTreeSubService getInstance(Vertx vertx, JsonObject config) {
    if (igniteAndSubTreeSubService == null) {
      synchronized (IgniteAndSubTreeSubService.class) {
        if (igniteAndSubTreeSubService == null) {
          igniteAndSubTreeSubService = new IgniteAndSubTreeSubService(vertx, config);
        }
      }
    }
    return igniteAndSubTreeSubService;
  }

  private final SubTree subTree;
  private final IgniteCache<SubscriptionKey, Subscription> exactSubscriptionCache;
  private final IgniteCache<SubscriptionKey, Subscription> wildcardSubscriptionCache;

  private IgniteAndSubTreeSubService(Vertx vertx, JsonObject config) {
    this.subTree = SubTree.subTree();
    Ignite ignite = IgniteUtil.getIgnite(vertx);
    this.exactSubscriptionCache = IgniteAssist.initExactSubscriptionCache(ignite, config);
    this.wildcardSubscriptionCache = IgniteAssist.initWildcardSubscriptionCache(ignite, config);
    vertx.executeBlocking(Uni.createFrom().emitter(uniEmitter -> {
      loadSubsToSubTreeAndInitContinuousQuery(exactSubscriptionCache);
      loadSubsToSubTreeAndInitContinuousQuery(wildcardSubscriptionCache);
      uniEmitter.complete(null);
    })).subscribe().with(ConsumerUtil.nothingToDo(), t -> LOGGER.error("Error occurred when load subscriptions to sub tree and init continuous query", t));
  }

  private void loadSubsToSubTreeAndInitContinuousQuery(IgniteCache<SubscriptionKey, Subscription> subCache) {
    ContinuousQuery<SubscriptionKey, Subscription> subsContinuousQuery = new ContinuousQuery<>();
    subsContinuousQuery.setInitialQuery(new ScanQuery<>());
    subsContinuousQuery.setLocalListener(cacheEntryEvents -> cacheEntryEvents.forEach(cacheEntryEvent -> {
      LOGGER.debug("Cache entry event received: {}", cacheEntryEvent.toString());
      switch (cacheEntryEvent.getEventType()) {
        case CREATED, UPDATED -> subTree.saveOrUpdateSubscription(cacheEntryEvent.getValue());
        case REMOVED ->
          subTree.removeSubscription(cacheEntryEvent.getValue().getSessionId(), cacheEntryEvent.getValue().getTopicFilter());
      }
    }));
    QueryCursor<Cache.Entry<SubscriptionKey, Subscription>> subQueryCursor = subCache.query(subsContinuousQuery);
    subQueryCursor.forEach(entry -> subTree.saveOrUpdateSubscription(entry.getValue()));
  }

  @Override
  public Future<Void> clearSubs(String sessionId) {
    QueryCursor<SubscriptionKey> exactSubscriptionCursor = exactSubscriptionCache
      .<Cache.Entry<SubscriptionKey, Subscription>, SubscriptionKey>query(new ScanQuery<>((key, value) -> key.getSessionId().equals(sessionId)), Cache.Entry::getKey);
    Set<SubscriptionKey> exactKeys = exactSubscriptionCursor.getAll().stream().collect(TreeSet::new, TreeSet::add, TreeSet::addAll);
    exactSubscriptionCache.removeAll(exactKeys);

    QueryCursor<SubscriptionKey> wildcardCursor = wildcardSubscriptionCache
      .<Cache.Entry<SubscriptionKey, Subscription>, SubscriptionKey>query(new ScanQuery<>((key, value) -> key.getSessionId().equals(sessionId)), Cache.Entry::getKey);
    Set<SubscriptionKey> wildcardKeys = wildcardCursor.getAll().stream().collect(TreeSet::new, TreeSet::add, TreeSet::addAll);
    wildcardSubscriptionCache.removeAll(wildcardKeys);
    return Future.succeededFuture();
  }

  @Override
  public Future<Boolean> saveOrUpdateSub(Subscription subscription) {
    if (!TopicUtil.containsWildcard(subscription.getTopicFilter())) {
      boolean ifAlreadyExist = exactSubscriptionCache.containsKey(subscription.getKey());
      exactSubscriptionCache.put(subscription.getKey(), subscription);
      return Future.succeededFuture(ifAlreadyExist);
    } else {
      boolean ifAlreadyExist = wildcardSubscriptionCache.containsKey(subscription.getKey());
      wildcardSubscriptionCache.put(subscription.getKey(), subscription);
      return Future.succeededFuture(ifAlreadyExist);
    }
  }

  @Override
  public Future<Boolean> removeSub(String sessionId, String topicFilter) {
    if (!TopicUtil.containsWildcard(topicFilter)) {
      return Future.succeededFuture(exactSubscriptionCache.remove(new SubscriptionKey(sessionId, topicFilter)));
    } else {
      return Future.succeededFuture(wildcardSubscriptionCache.remove(new SubscriptionKey(sessionId, topicFilter)));
    }
  }

  @Override
  public Future<List<Subscription>> allMatchSubs(String topicName, boolean distinct) {
    return Future.succeededFuture(subTree.findAllMatch(topicName, distinct));
  }

  @Override
  public Future<List<Subscription>> allMatchExactSubs(String topicName) {
    return Future.succeededFuture(subTree.findAllMatch(topicName, false).stream().filter(subscription -> !TopicUtil.containsWildcard(subscription.getTopicFilter())).collect(Collectors.toList()));
  }

  @Override
  public Future<List<Subscription>> allMatchWildcardSubs(String topicName, boolean distinct) {
    return Future.succeededFuture(subTree.findAllMatch(topicName, distinct).stream().filter(subscription -> TopicUtil.containsWildcard(subscription.getTopicFilter())).collect(Collectors.toList()));
  }

  @Override
  public Future<List<Subscription>> allExactSubs() {
    return Future.succeededFuture(subTree.allSubs().stream().filter(subscription -> !TopicUtil.containsWildcard(subscription.getTopicFilter())).collect(Collectors.toList()));
  }

  @Override
  public Future<List<Subscription>> allWildcardSubs() {
    return Future.succeededFuture(subTree.allSubs().stream().filter(subscription -> TopicUtil.containsWildcard(subscription.getTopicFilter())).collect(Collectors.toList()));
  }

  @Override
  public Future<List<Subscription>> allSubs() {
    return Future.succeededFuture(subTree.allSubs());
  }

  @Override
  public Future<Long> countMatchExactSubs(String topicName) {
    return allMatchExactSubs(topicName).map(subscriptions -> (long) subscriptions.size());
  }

  @Override
  public Future<Long> countMatchWildcardSubs(String topicName, boolean distinct) {
    return allMatchWildcardSubs(topicName, distinct).map(subscriptions -> (long) subscriptions.size());
  }

  @Override
  public Future<Long> countMatch(String topicName, boolean distinct) {
    return allMatchSubs(topicName, distinct).map(subscriptions -> (long) subscriptions.size());
  }

  @Override
  public Future<Long> countExactSubs() {
    return allExactSubs().map(subscriptions -> (long) subscriptions.size());
  }

  @Override
  public Future<Long> countWildcardSubs() {
    return allWildcardSubs().map(subscriptions -> (long) subscriptions.size());
  }

  @Override
  public Future<Long> count() {
    return allSubs().map(subscriptions -> (long) subscriptions.size());
  }
}
