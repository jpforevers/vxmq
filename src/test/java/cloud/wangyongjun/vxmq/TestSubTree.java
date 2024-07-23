package cloud.wangyongjun.vxmq;

import cloud.wangyongjun.vxmq.service.sub.Subscription;
import cloud.wangyongjun.vxmq.service.sub.tree.SubTree;
import cloud.wangyongjun.vxmq.service.sub.tree.impl.SubTreeTrieAndRecursiveImpl;
import io.smallrye.mutiny.Uni;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import io.vertx.mutiny.core.Vertx;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(VertxExtension.class)
public class TestSubTree {

  @Test
  void testSaveOrUpdateSub(Vertx vertx, VertxTestContext testContext) throws Throwable {
    Uni.createFrom().voidItem()
      .onItem().transformToUni(v -> {
        Subscription s1 = new Subscription().setSessionId("s1").setClientId("c1").setTopicFilter("abc/def/123").setQos(1).setCreatedTime(Instant.now().toEpochMilli());
        Subscription s2 = new Subscription().setSessionId("s2").setClientId("c2").setTopicFilter("+/def/123").setQos(1).setCreatedTime(Instant.now().toEpochMilli());
        Subscription s3 = new Subscription().setSessionId("s3").setClientId("c3").setTopicFilter("abc/+/123").setQos(1).setCreatedTime(Instant.now().toEpochMilli());
        Subscription s4 = new Subscription().setSessionId("s4").setClientId("c4").setTopicFilter("abc/def/+").setQos(1).setCreatedTime(Instant.now().toEpochMilli());
        Subscription s5 = new Subscription().setSessionId("s5").setClientId("c5").setTopicFilter("#").setQos(1).setCreatedTime(Instant.now().toEpochMilli());
        Subscription s6 = new Subscription().setSessionId("s6").setClientId("c6").setTopicFilter("abc/#").setQos(2).setCreatedTime(Instant.now().toEpochMilli());
        Subscription s7 = new Subscription().setSessionId("s7").setClientId("c7").setTopicFilter("abc/def/#").setQos(2).setCreatedTime(Instant.now().toEpochMilli());
        Subscription s8 = new Subscription().setSessionId("s8").setClientId("c8").setTopicFilter("abc/def/123/#").setQos(0).setCreatedTime(Instant.now().toEpochMilli());
        Subscription s9 = new Subscription().setSessionId("s9").setClientId("c9").setTopicFilter("+/+/123").setQos(1).setCreatedTime(Instant.now().toEpochMilli());
        Subscription s10 = new Subscription().setSessionId("s10").setClientId("c10").setTopicFilter("abc/+/+").setQos(1).setCreatedTime(Instant.now().toEpochMilli());
        Subscription s11 = new Subscription().setSessionId("s11").setClientId("c11").setTopicFilter("+/def/+").setQos(1).setCreatedTime(Instant.now().toEpochMilli());
        Subscription s12 = new Subscription().setSessionId("s12").setClientId("c12").setTopicFilter("+/+/+").setQos(1).setCreatedTime(Instant.now().toEpochMilli());

        SubTree subTree = new SubTreeTrieAndRecursiveImpl();
        subTree.saveOrUpdateSubscription(s1);subTree.saveOrUpdateSubscription(s2);subTree.saveOrUpdateSubscription(s3);
        subTree.saveOrUpdateSubscription(s4);subTree.saveOrUpdateSubscription(s5);subTree.saveOrUpdateSubscription(s6);
        subTree.saveOrUpdateSubscription(s7);subTree.saveOrUpdateSubscription(s8);subTree.saveOrUpdateSubscription(s9);
        subTree.saveOrUpdateSubscription(s10);subTree.saveOrUpdateSubscription(s11);subTree.saveOrUpdateSubscription(s12);

        subTree.saveOrUpdateSubscription(s10);subTree.saveOrUpdateSubscription(s11);subTree.saveOrUpdateSubscription(s12);

        assertEquals(Set.of(s1, s2, s3, s4, s5, s6, s7, s8, s9, s10, s11, s12), new HashSet<>(subTree.allSubs()));
        return Uni.createFrom().voidItem();
      })
      .subscribe().with(v -> testContext.completeNow(), Throwable::printStackTrace);
  }

  @Test
  void testMatchRule(Vertx vertx, VertxTestContext testContext) throws Throwable {
    Uni.createFrom().voidItem()
      .onItem().transformToUni(v -> {
        Subscription s1 = new Subscription().setSessionId("s1").setClientId("c1").setTopicFilter("abc/def/123").setQos(1).setCreatedTime(Instant.now().toEpochMilli());
        Subscription s2 = new Subscription().setSessionId("s2").setClientId("c2").setTopicFilter("+/def/123").setQos(1).setCreatedTime(Instant.now().toEpochMilli());
        Subscription s3 = new Subscription().setSessionId("s3").setClientId("c3").setTopicFilter("abc/+/123").setQos(1).setCreatedTime(Instant.now().toEpochMilli());
        Subscription s4 = new Subscription().setSessionId("s4").setClientId("c4").setTopicFilter("abc/def/+").setQos(1).setCreatedTime(Instant.now().toEpochMilli());
        Subscription s5 = new Subscription().setSessionId("s5").setClientId("c5").setTopicFilter("#").setQos(1).setCreatedTime(Instant.now().toEpochMilli());
        Subscription s6 = new Subscription().setSessionId("s6").setClientId("c6").setTopicFilter("abc/#").setQos(2).setCreatedTime(Instant.now().toEpochMilli());
        Subscription s7 = new Subscription().setSessionId("s7").setClientId("c7").setTopicFilter("abc/def/#").setQos(2).setCreatedTime(Instant.now().toEpochMilli());
        Subscription s8 = new Subscription().setSessionId("s8").setClientId("c8").setTopicFilter("abc/def/123/#").setQos(0).setCreatedTime(Instant.now().toEpochMilli());
        Subscription s9 = new Subscription().setSessionId("s9").setClientId("c9").setTopicFilter("+/+/123").setQos(1).setCreatedTime(Instant.now().toEpochMilli());
        Subscription s10 = new Subscription().setSessionId("s10").setClientId("c10").setTopicFilter("abc/+/+").setQos(1).setCreatedTime(Instant.now().toEpochMilli());
        Subscription s11 = new Subscription().setSessionId("s11").setClientId("c11").setTopicFilter("+/def/+").setQos(1).setCreatedTime(Instant.now().toEpochMilli());
        Subscription s12 = new Subscription().setSessionId("s12").setClientId("c12").setTopicFilter("+/+/+").setQos(1).setCreatedTime(Instant.now().toEpochMilli());

        SubTree subTree = new SubTreeTrieAndRecursiveImpl();
        subTree.saveOrUpdateSubscription(s1);subTree.saveOrUpdateSubscription(s2);subTree.saveOrUpdateSubscription(s3);
        subTree.saveOrUpdateSubscription(s4);subTree.saveOrUpdateSubscription(s5);subTree.saveOrUpdateSubscription(s6);
        subTree.saveOrUpdateSubscription(s7);subTree.saveOrUpdateSubscription(s8);subTree.saveOrUpdateSubscription(s9);
        subTree.saveOrUpdateSubscription(s10);subTree.saveOrUpdateSubscription(s11);subTree.saveOrUpdateSubscription(s12);

        Set<Subscription> allMatch = new HashSet<>(subTree.findAllMatch("abc/def/123", false));
        System.out.println(allMatch);
        assertEquals(Set.of(s1, s2, s3, s4, s5, s6, s7, s8, s9, s10, s11, s12), allMatch);
        return Uni.createFrom().voidItem();
      })
      .subscribe().with(v -> testContext.completeNow(), Throwable::printStackTrace);
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
        System.out.println(allMatch);
        assertEquals(Set.of(s3, s4), allMatch);
        return Uni.createFrom().voidItem();
      })
      .subscribe().with(v -> testContext.completeNow(), Throwable::printStackTrace);
  }

  @Test
  void testRemoveSub(Vertx vertx, VertxTestContext testContext) throws Throwable {
    Uni.createFrom().voidItem()
      .onItem().transformToUni(v -> {
        Subscription s1 = new Subscription().setSessionId("s1").setClientId("c1").setTopicFilter("abc/def/123").setQos(1).setCreatedTime(Instant.now().toEpochMilli());
        Subscription s2 = new Subscription().setSessionId("s2").setClientId("c2").setTopicFilter("+/def/123").setQos(1).setCreatedTime(Instant.now().toEpochMilli());
        Subscription s3 = new Subscription().setSessionId("s3").setClientId("c3").setTopicFilter("abc/+/123").setQos(1).setCreatedTime(Instant.now().toEpochMilli());
        Subscription s4 = new Subscription().setSessionId("s4").setClientId("c4").setTopicFilter("abc/def/+").setQos(1).setCreatedTime(Instant.now().toEpochMilli());
        Subscription s5 = new Subscription().setSessionId("s5").setClientId("c5").setTopicFilter("#").setQos(1).setCreatedTime(Instant.now().toEpochMilli());
        Subscription s6 = new Subscription().setSessionId("s6").setClientId("c6").setTopicFilter("abc/#").setQos(2).setCreatedTime(Instant.now().toEpochMilli());
        Subscription s7 = new Subscription().setSessionId("s7").setClientId("c7").setTopicFilter("abc/def/#").setQos(2).setCreatedTime(Instant.now().toEpochMilli());
        Subscription s8 = new Subscription().setSessionId("s8").setClientId("c8").setTopicFilter("abc/def/123/#").setQos(0).setCreatedTime(Instant.now().toEpochMilli());
        Subscription s9 = new Subscription().setSessionId("s9").setClientId("c9").setTopicFilter("+/+/123").setQos(1).setCreatedTime(Instant.now().toEpochMilli());
        Subscription s10 = new Subscription().setSessionId("s10").setClientId("c10").setTopicFilter("abc/+/+").setQos(1).setCreatedTime(Instant.now().toEpochMilli());
        Subscription s11 = new Subscription().setSessionId("s11").setClientId("c11").setTopicFilter("+/def/+").setQos(1).setCreatedTime(Instant.now().toEpochMilli());
        Subscription s12 = new Subscription().setSessionId("s12").setClientId("c12").setTopicFilter("+/+/+").setQos(1).setCreatedTime(Instant.now().toEpochMilli());

        SubTree subTree = new SubTreeTrieAndRecursiveImpl();
        subTree.saveOrUpdateSubscription(s1);subTree.saveOrUpdateSubscription(s2);subTree.saveOrUpdateSubscription(s3);
        subTree.saveOrUpdateSubscription(s4);subTree.saveOrUpdateSubscription(s5);subTree.saveOrUpdateSubscription(s6);
        subTree.saveOrUpdateSubscription(s7);subTree.saveOrUpdateSubscription(s8);subTree.saveOrUpdateSubscription(s9);
        subTree.saveOrUpdateSubscription(s10);subTree.saveOrUpdateSubscription(s11);subTree.saveOrUpdateSubscription(s12);

        subTree.removeSubscription(s1.getSessionId(), s1.getTopicFilter());
        subTree.removeSubscription(s12.getSessionId(), s12.getTopicFilter());

        assertEquals(Set.of(s2, s3, s4, s5, s6, s7, s8, s9, s10, s11), new HashSet<>(subTree.allSubs()));
        return Uni.createFrom().voidItem();
      })
      .subscribe().with(v -> testContext.completeNow(), Throwable::printStackTrace);
  }

  @Test
  void testClearSub(Vertx vertx, VertxTestContext testContext) throws Throwable {
    Uni.createFrom().voidItem()
      .onItem().transformToUni(v -> {
        Subscription s1 = new Subscription().setSessionId("s1").setClientId("c1").setTopicFilter("abc/def/123").setQos(1).setCreatedTime(Instant.now().toEpochMilli());
        Subscription s2 = new Subscription().setSessionId("s2").setClientId("c2").setTopicFilter("+/def/123").setQos(1).setCreatedTime(Instant.now().toEpochMilli());
        Subscription s3 = new Subscription().setSessionId("s3").setClientId("c3").setTopicFilter("abc/+/123").setQos(1).setCreatedTime(Instant.now().toEpochMilli());
        Subscription s4 = new Subscription().setSessionId("s4").setClientId("c4").setTopicFilter("abc/def/+").setQos(1).setCreatedTime(Instant.now().toEpochMilli());
        Subscription s5 = new Subscription().setSessionId("s5").setClientId("c5").setTopicFilter("#").setQos(1).setCreatedTime(Instant.now().toEpochMilli());
        Subscription s6 = new Subscription().setSessionId("s6").setClientId("c6").setTopicFilter("abc/#").setQos(2).setCreatedTime(Instant.now().toEpochMilli());
        Subscription s7 = new Subscription().setSessionId("s7").setClientId("c7").setTopicFilter("abc/def/#").setQos(2).setCreatedTime(Instant.now().toEpochMilli());
        Subscription s8 = new Subscription().setSessionId("s8").setClientId("c8").setTopicFilter("abc/def/123/#").setQos(0).setCreatedTime(Instant.now().toEpochMilli());
        Subscription s9 = new Subscription().setSessionId("s9").setClientId("c9").setTopicFilter("+/+/123").setQos(1).setCreatedTime(Instant.now().toEpochMilli());
        Subscription s10 = new Subscription().setSessionId("s10").setClientId("c10").setTopicFilter("abc/+/+").setQos(1).setCreatedTime(Instant.now().toEpochMilli());
        Subscription s11 = new Subscription().setSessionId("s11").setClientId("c11").setTopicFilter("+/def/+").setQos(1).setCreatedTime(Instant.now().toEpochMilli());
        Subscription s12 = new Subscription().setSessionId("s12").setClientId("c12").setTopicFilter("+/+/+").setQos(1).setCreatedTime(Instant.now().toEpochMilli());

        Subscription s13 = new Subscription().setSessionId("s13").setClientId("c13").setTopicFilter("abc/def/123").setQos(1).setCreatedTime(Instant.now().toEpochMilli());
        Subscription s14 = new Subscription().setSessionId("s13").setClientId("c13").setTopicFilter("abc/def/456").setQos(1).setCreatedTime(Instant.now().toEpochMilli());
        Subscription s15 = new Subscription().setSessionId("s13").setClientId("c13").setTopicFilter("abc/def/789").setQos(1).setCreatedTime(Instant.now().toEpochMilli());

        SubTree subTree = new SubTreeTrieAndRecursiveImpl();
        subTree.saveOrUpdateSubscription(s1);subTree.saveOrUpdateSubscription(s2);subTree.saveOrUpdateSubscription(s3);
        subTree.saveOrUpdateSubscription(s4);subTree.saveOrUpdateSubscription(s5);subTree.saveOrUpdateSubscription(s6);
        subTree.saveOrUpdateSubscription(s7);subTree.saveOrUpdateSubscription(s8);subTree.saveOrUpdateSubscription(s9);
        subTree.saveOrUpdateSubscription(s10);subTree.saveOrUpdateSubscription(s11);subTree.saveOrUpdateSubscription(s12);
        subTree.saveOrUpdateSubscription(s13);subTree.saveOrUpdateSubscription(s14);subTree.saveOrUpdateSubscription(s15);

        subTree.clearSubscription(s13.getSessionId());

        assertEquals(Set.of(s1, s2, s3, s4, s5, s6, s7, s8, s9, s10, s11, s12), new HashSet<>(subTree.allSubs()));
        return Uni.createFrom().voidItem();
      })
      .subscribe().with(v -> testContext.completeNow(), Throwable::printStackTrace);
  }

}
