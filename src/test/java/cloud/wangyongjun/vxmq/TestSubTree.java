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

package cloud.wangyongjun.vxmq;

import cloud.wangyongjun.vxmq.service.sub.Subscription;
import cloud.wangyongjun.vxmq.service.sub.tree.SubTree;
import cloud.wangyongjun.vxmq.service.sub.tree.impl.SubTreeTrieAndRecursiveImpl;
import io.smallrye.mutiny.Uni;
import io.vertx.junit5.VertxTestContext;
import io.vertx.mutiny.core.Vertx;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestSubTree extends BaseTest {

  private static final Logger LOGGER = LoggerFactory.getLogger(TestSubTree.class);

  @Test
  void testSaveOrUpdateSub(Vertx vertx, VertxTestContext testContext) throws Throwable {
    Uni.createFrom().voidItem()
      .onItem().transformToUni(v -> {
        SubTree subTree = new SubTreeTrieAndRecursiveImpl();
        for (Subscription subscription : TestConstants.SUBSCRIPTIONS) {
          subTree.saveOrUpdateSubscription(subscription);
        }

        subTree.saveOrUpdateSubscription(TestConstants.SUBSCRIPTIONS.get(9));
        subTree.saveOrUpdateSubscription(TestConstants.SUBSCRIPTIONS.get(10));
        subTree.saveOrUpdateSubscription(TestConstants.SUBSCRIPTIONS.get(11));

        assertEquals(new HashSet<>(TestConstants.SUBSCRIPTIONS), new HashSet<>(subTree.allSubs()));
        LOGGER.info("All subscriptions saved successfully");
        return Uni.createFrom().voidItem();
      })
      .subscribe().with(v -> testContext.completeNow(), testContext::failNow);
  }

  @Test
  void testMatchRule(Vertx vertx, VertxTestContext testContext) throws Throwable {
    Uni.createFrom().voidItem()
      .onItem().transformToUni(v -> {
        SubTree subTree = new SubTreeTrieAndRecursiveImpl();
        for (Subscription subscription : TestConstants.SUBSCRIPTIONS) {
          subTree.saveOrUpdateSubscription(subscription);
        }

        String topicName = "abc/def/123";
        Set<Subscription> allMatch = new HashSet<>(subTree.findAllMatch(topicName, false));

        assertEquals(new HashSet<>(TestConstants.SUBSCRIPTIONS), allMatch);
        LOGGER.info("All subscriptions matching {}", topicName);
        return Uni.createFrom().voidItem();
      })
      .subscribe().with(v -> testContext.completeNow(), testContext::failNow);
  }


  /**
   * The Server MUST NOT match Topic Filters starting with a wildcard character (# or +) with Topic Names beginning with a $ character.
   * <a href="https://docs.oasis-open.org/mqtt/mqtt/v5.0/os/mqtt-v5.0-os.html#_Toc3901246">MQTT Spec - 4.7.2 Topics beginning with $</a>
   */
  @Test
  void testMatchRuleWith$(Vertx vertx, VertxTestContext testContext) throws Throwable {
    Uni.createFrom().voidItem()
      .onItem().transformToUni(v -> {
        // A subscription to “#” will not receive any messages published to a topic beginning with a $
        Subscription s1 = new Subscription().setSessionId("s1").setClientId("c1").setTopicFilter("#").setQos(1).setCreatedTime(Instant.now().toEpochMilli());
        // A subscription to “+/monitor/Clients” will not receive any messages published to “$SYS/monitor/Clients”
        Subscription s2 = new Subscription().setSessionId("s2").setClientId("c2").setTopicFilter("+/monitor/Clients").setQos(1).setCreatedTime(Instant.now().toEpochMilli());
        // A subscription to “$SYS/#” will receive messages published to topics beginning with “$SYS/”
        Subscription s3 = new Subscription().setSessionId("s3").setClientId("c3").setTopicFilter("$SYS/#").setQos(1).setCreatedTime(Instant.now().toEpochMilli());
        // A subscription to “$SYS/monitor/+” will receive messages published to “$SYS/monitor/Clients”
        Subscription s4 = new Subscription().setSessionId("s4").setClientId("c4").setTopicFilter("$SYS/monitor/+").setQos(1).setCreatedTime(Instant.now().toEpochMilli());

        SubTree subTree = new SubTreeTrieAndRecursiveImpl();
        subTree.saveOrUpdateSubscription(s1);
        subTree.saveOrUpdateSubscription(s2);
        subTree.saveOrUpdateSubscription(s3);
        subTree.saveOrUpdateSubscription(s4);

        Set<Subscription> allMatch = new HashSet<>(subTree.findAllMatch("$SYS/monitor/Clients", false));
        assertEquals(Set.of(s3, s4), allMatch);
        return Uni.createFrom().voidItem();
      })
      .subscribe().with(v -> testContext.completeNow(), testContext::failNow);
  }

  @Test
  void testRemoveSub(Vertx vertx, VertxTestContext testContext) throws Throwable {
    Uni.createFrom().voidItem()
      .onItem().transformToUni(v -> {
        SubTree subTree = new SubTreeTrieAndRecursiveImpl();
        for (Subscription subscription : TestConstants.SUBSCRIPTIONS) {
          subTree.saveOrUpdateSubscription(subscription);
        }

        subTree.removeSubscription(TestConstants.SUBSCRIPTIONS.get(0).getSessionId(), TestConstants.SUBSCRIPTIONS.get(0).getTopicFilter());
        subTree.removeSubscription(TestConstants.SUBSCRIPTIONS.get(11).getSessionId(), TestConstants.SUBSCRIPTIONS.get(11).getTopicFilter());

        assertEquals(Set.of(TestConstants.SUBSCRIPTIONS.get(1), TestConstants.SUBSCRIPTIONS.get(2), TestConstants.SUBSCRIPTIONS.get(3),
          TestConstants.SUBSCRIPTIONS.get(4), TestConstants.SUBSCRIPTIONS.get(5), TestConstants.SUBSCRIPTIONS.get(6),
          TestConstants.SUBSCRIPTIONS.get(7), TestConstants.SUBSCRIPTIONS.get(8), TestConstants.SUBSCRIPTIONS.get(9),
          TestConstants.SUBSCRIPTIONS.get(10)), new HashSet<>(subTree.allSubs()));
        return Uni.createFrom().voidItem();
      })
      .subscribe().with(v -> testContext.completeNow(), testContext::failNow);
  }

  @Test
  void testClearSub(Vertx vertx, VertxTestContext testContext) throws Throwable {
    Uni.createFrom().voidItem()
      .onItem().transformToUni(v -> {
        SubTree subTree = new SubTreeTrieAndRecursiveImpl();
        for (Subscription subscription : TestConstants.SUBSCRIPTIONS) {
          subTree.saveOrUpdateSubscription(subscription);
        }

        Subscription s13 = new Subscription().setSessionId("s13").setClientId("c13").setTopicFilter("abc/def/123").setQos(1).setCreatedTime(Instant.now().toEpochMilli());
        Subscription s14 = new Subscription().setSessionId("s13").setClientId("c13").setTopicFilter("abc/def/456").setQos(1).setCreatedTime(Instant.now().toEpochMilli());
        Subscription s15 = new Subscription().setSessionId("s13").setClientId("c13").setTopicFilter("abc/def/789").setQos(1).setCreatedTime(Instant.now().toEpochMilli());

        subTree.saveOrUpdateSubscription(s13);
        subTree.saveOrUpdateSubscription(s14);
        subTree.saveOrUpdateSubscription(s15);

        subTree.clearSubscription(s13.getSessionId());

        assertEquals(new HashSet<>(TestConstants.SUBSCRIPTIONS), new HashSet<>(subTree.allSubs()));
        return Uni.createFrom().voidItem();
      })
      .subscribe().with(v -> testContext.completeNow(), testContext::failNow);
  }

}
