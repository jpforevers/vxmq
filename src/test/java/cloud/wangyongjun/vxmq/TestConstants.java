package cloud.wangyongjun.vxmq;

import cloud.wangyongjun.vxmq.service.sub.Subscription;

import java.time.Instant;
import java.util.List;

public class TestConstants {

  public static final List<Subscription> SUBSCRIPTIONS = List.of(
    new Subscription().setSessionId("s1").setClientId("c1").setTopicFilter("abc/def/123").setQos(1).setCreatedTime(Instant.now().toEpochMilli()),
    new Subscription().setSessionId("s2").setClientId("c2").setTopicFilter("+/def/123").setQos(1).setCreatedTime(Instant.now().toEpochMilli()),
    new Subscription().setSessionId("s3").setClientId("c3").setTopicFilter("abc/+/123").setQos(1).setCreatedTime(Instant.now().toEpochMilli()),
    new Subscription().setSessionId("s4").setClientId("c4").setTopicFilter("abc/def/+").setQos(1).setCreatedTime(Instant.now().toEpochMilli()),
    new Subscription().setSessionId("s5").setClientId("c5").setTopicFilter("#").setQos(1).setCreatedTime(Instant.now().toEpochMilli()),
    new Subscription().setSessionId("s6").setClientId("c6").setTopicFilter("abc/#").setQos(2).setCreatedTime(Instant.now().toEpochMilli()),
    new Subscription().setSessionId("s7").setClientId("c7").setTopicFilter("abc/def/#").setQos(2).setCreatedTime(Instant.now().toEpochMilli()),
    new Subscription().setSessionId("s8").setClientId("c8").setTopicFilter("abc/def/123/#").setQos(0).setCreatedTime(Instant.now().toEpochMilli()),
    new Subscription().setSessionId("s9").setClientId("c9").setTopicFilter("+/+/123").setQos(1).setCreatedTime(Instant.now().toEpochMilli()),
    new Subscription().setSessionId("s10").setClientId("c10").setTopicFilter("abc/+/+").setQos(1).setCreatedTime(Instant.now().toEpochMilli()),
    new Subscription().setSessionId("s11").setClientId("c11").setTopicFilter("+/def/+").setQos(1).setCreatedTime(Instant.now().toEpochMilli()),
    new Subscription().setSessionId("s12").setClientId("c12").setTopicFilter("+/+/+").setQos(1).setCreatedTime(Instant.now().toEpochMilli())
  );

}
