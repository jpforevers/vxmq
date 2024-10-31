/*
 * Copyright (C) 2023-2024 王用军
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package cloud.wangyongjun.vxmq.service.sub;

import cloud.wangyongjun.vxmq.assist.ConsumerUtil;
import cloud.wangyongjun.vxmq.service.HazelcastAssist;
import cloud.wangyongjun.vxmq.service.HazelcastUtil;
import cloud.wangyongjun.vxmq.assist.TopicUtil;
import cloud.wangyongjun.vxmq.service.sub.share.ShareSubscriptionProcessor;
import cloud.wangyongjun.vxmq.service.sub.tree.SubTree;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;
import com.hazelcast.map.listener.*;
import io.vertx.core.Future;
import io.vertx.mutiny.core.Vertx;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Singleton. SubTree is not thread safe.
 */
public class HazelcastAndSubTreeSubService implements SubService {

  private final static Logger LOGGER = LoggerFactory.getLogger(HazelcastAndSubTreeSubService.class);

  private static volatile HazelcastAndSubTreeSubService hazelcastAndSubTreeSubService;

  public static HazelcastAndSubTreeSubService getInstance(Vertx vertx, ShareSubscriptionProcessor shareSubscriptionProcessor) {
    if (hazelcastAndSubTreeSubService == null) {
      synchronized (HazelcastAndSubTreeSubService.class) {
        if (hazelcastAndSubTreeSubService == null) {
          hazelcastAndSubTreeSubService = new HazelcastAndSubTreeSubService(vertx, shareSubscriptionProcessor);
        }
      }
    }
    return hazelcastAndSubTreeSubService;
  }

  private final SubTree subTree;
  private final IMap<SubscriptionKey, Subscription> exactSubscriptionCache;
  private final IMap<SubscriptionKey, Subscription> wildcardSubscriptionCache;
  private final ShareSubscriptionProcessor shareSubscriptionProcessor;

  private HazelcastAndSubTreeSubService(Vertx vertx, ShareSubscriptionProcessor shareSubscriptionProcessor) {
    this.subTree = SubTree.subTree();
    HazelcastInstance hazelcastInstance = HazelcastUtil.getHazelcastInstance(vertx);
    this.exactSubscriptionCache = HazelcastAssist.initExactSubscriptionCache(hazelcastInstance);
    this.wildcardSubscriptionCache = HazelcastAssist.initWildcardSubscriptionCache(hazelcastInstance);
    this.shareSubscriptionProcessor = shareSubscriptionProcessor;
    vertx.<Void>executeBlocking(() -> {
      loadSubsToSubTreeAndInitContinuousQuery(exactSubscriptionCache);
      loadSubsToSubTreeAndInitContinuousQuery(wildcardSubscriptionCache);
      return null;
    }).subscribe().with(ConsumerUtil.nothingToDo(), t -> LOGGER.error("Error occurred when load subscriptions to sub tree and init continuous query", t));
  }

  private void loadSubsToSubTreeAndInitContinuousQuery(IMap<SubscriptionKey, Subscription> subCache) {
    subCache.addEntryListener((EntryAddedListener<SubscriptionKey, Subscription>) event -> {
      subTree.saveOrUpdateSubscription(event.getValue());
    }, true);
    subCache.addEntryListener((EntryUpdatedListener<SubscriptionKey, Subscription>) event -> {
      subTree.saveOrUpdateSubscription(event.getValue());
    }, true);
    subCache.addEntryListener((EntryLoadedListener<SubscriptionKey, Subscription>) event -> {
      subTree.saveOrUpdateSubscription(event.getValue());
    }, true);
    subCache.addEntryListener((EntryEvictedListener<SubscriptionKey, Subscription>) event -> {
      subTree.removeSubscription(event.getKey().getSessionId(), event.getValue().getTopicFilter());
    }, true);
    subCache.addEntryListener((EntryExpiredListener<SubscriptionKey, Subscription>) event -> {
      subTree.removeSubscription(event.getKey().getSessionId(), event.getValue().getTopicFilter());
    }, true);
    subCache.addEntryListener((EntryRemovedListener<SubscriptionKey, Subscription>) event -> {
      subTree.removeSubscription(event.getKey().getSessionId(), event.getOldValue().getTopicFilter());
    }, true);
    subCache.values().forEach(subTree::saveOrUpdateSubscription);
  }

  @Override
  public Future<Void> clearSubs(String sessionId) {
    clearSubs(sessionId, exactSubscriptionCache);
    clearSubs(sessionId, wildcardSubscriptionCache);
    return Future.succeededFuture();
  }

  private void clearSubs(String sessionId, IMap<SubscriptionKey, Subscription> subscriptionCache) {
    subscriptionCache.removeAll(mapEntry -> mapEntry.getKey().getSessionId().equals(sessionId));
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
      return Future.succeededFuture(exactSubscriptionCache.remove(new SubscriptionKey(sessionId, topicFilter)) != null);
    } else {
      return Future.succeededFuture(wildcardSubscriptionCache.remove(new SubscriptionKey(sessionId, topicFilter)) != null);
    }
  }

  @Override
  public Future<List<Subscription>> allMatchSubs(String topicName, boolean distinct) {
    List<Subscription> allMatchSubscriptions = subTree.findAllMatch(topicName, distinct);
    return Future.succeededFuture(shareSubscriptionProcessor.process(allMatchSubscriptions));
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
