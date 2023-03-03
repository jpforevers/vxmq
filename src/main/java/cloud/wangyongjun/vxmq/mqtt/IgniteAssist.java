package cloud.wangyongjun.vxmq.mqtt;

import cloud.wangyongjun.vxmq.assist.Config;
import cloud.wangyongjun.vxmq.mqtt.msg.*;
import cloud.wangyongjun.vxmq.mqtt.retain.Retain;
import cloud.wangyongjun.vxmq.mqtt.session.Session;
import cloud.wangyongjun.vxmq.mqtt.sub.Subscription;
import cloud.wangyongjun.vxmq.mqtt.sub.SubscriptionKey;
import cloud.wangyongjun.vxmq.mqtt.will.Will;
import io.vertx.core.json.JsonObject;
import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteCache;
import org.apache.ignite.IgniteQueue;
import org.apache.ignite.cache.CacheAtomicityMode;
import org.apache.ignite.cache.CacheMode;
import org.apache.ignite.cache.CacheWriteSynchronizationMode;
import org.apache.ignite.configuration.CacheConfiguration;
import org.apache.ignite.configuration.CollectionConfiguration;

public class IgniteAssist {

  public static final String INBOUND_QOS2_PUB_CACHE_NAME = "inboundQos2PubCache";
  public static final String OUTBOUND_QOS1_PUB_CACHE_NAME = "outboundQos1PubCache";
  public static final String OUTBOUND_QOS2_PUB_CACHE_NAME = "outboundQos2PubCache";
  public static final String OUTBOUND_QOS2_REL_CACHE_NAME = "outboundQos2RelCache";
  public static final String RETAIN_CACHE_NAME = "retainCache";
  public static final String SESSION_CACHE_NAME = "sessionCache";
  public static final String EXACT_SUBSCRIPTION_CACHE_NAME = "exactSubscriptionCache";
  public static final String WILDCARD_SUBSCRIPTION_CACHE_NAME = "wildcardSubscriptionCache";
  public static final String WILL_CACHE_NAME = "willCache";
  public static final String OFFLINE_MSG_QUEUE_PREFIX = "OFFLINE_MSG_QUEUE_";

  public static IgniteCache<InboundQos2PubKey, InboundQos2Pub> initInboundQos2PubCache(Ignite ignite) {
    CacheConfiguration<InboundQos2PubKey, InboundQos2Pub> inboundQos2PubCacheConfiguration = new CacheConfiguration<>();
    inboundQos2PubCacheConfiguration.setName(IgniteAssist.INBOUND_QOS2_PUB_CACHE_NAME);
    inboundQos2PubCacheConfiguration.setWriteSynchronizationMode(CacheWriteSynchronizationMode.FULL_ASYNC);
    inboundQos2PubCacheConfiguration.setCacheMode(CacheMode.PARTITIONED);
    inboundQos2PubCacheConfiguration.setAtomicityMode(CacheAtomicityMode.ATOMIC);
    return ignite.getOrCreateCache(inboundQos2PubCacheConfiguration);
  }

  public static IgniteCache<OutboundQos1PubKey, OutboundQos1Pub> initOutboundQos1PubCache(Ignite ignite) {
    CacheConfiguration<OutboundQos1PubKey, OutboundQos1Pub> outboundQos1PubCacheConfiguration = new CacheConfiguration<>();
    outboundQos1PubCacheConfiguration.setName(IgniteAssist.OUTBOUND_QOS1_PUB_CACHE_NAME);
    outboundQos1PubCacheConfiguration.setWriteSynchronizationMode(CacheWriteSynchronizationMode.FULL_ASYNC);
    outboundQos1PubCacheConfiguration.setCacheMode(CacheMode.PARTITIONED);
    outboundQos1PubCacheConfiguration.setAtomicityMode(CacheAtomicityMode.ATOMIC);
    return ignite.getOrCreateCache(outboundQos1PubCacheConfiguration);

  }

  public static IgniteCache<OutboundQos2PubKey, OutboundQos2Pub> initOutboundQos2PubCache(Ignite ignite) {
    CacheConfiguration<OutboundQos2PubKey, OutboundQos2Pub> outboundQos2PubCacheConfiguration = new CacheConfiguration<>();
    outboundQos2PubCacheConfiguration.setName(IgniteAssist.OUTBOUND_QOS2_PUB_CACHE_NAME);
    outboundQos2PubCacheConfiguration.setWriteSynchronizationMode(CacheWriteSynchronizationMode.FULL_ASYNC);
    outboundQos2PubCacheConfiguration.setCacheMode(CacheMode.PARTITIONED);
    outboundQos2PubCacheConfiguration.setAtomicityMode(CacheAtomicityMode.ATOMIC);
    return ignite.getOrCreateCache(outboundQos2PubCacheConfiguration);
  }

  public static IgniteCache<OutboundQos2RelKey, OutboundQos2Rel> initOutboundQos2RelCache(Ignite ignite) {
    CacheConfiguration<OutboundQos2RelKey, OutboundQos2Rel> outboundQos2RelCacheConfiguration = new CacheConfiguration<>();
    outboundQos2RelCacheConfiguration.setName(IgniteAssist.OUTBOUND_QOS2_REL_CACHE_NAME);
    outboundQos2RelCacheConfiguration.setWriteSynchronizationMode(CacheWriteSynchronizationMode.FULL_ASYNC);
    outboundQos2RelCacheConfiguration.setCacheMode(CacheMode.PARTITIONED);
    outboundQos2RelCacheConfiguration.setAtomicityMode(CacheAtomicityMode.ATOMIC);
    return ignite.getOrCreateCache(outboundQos2RelCacheConfiguration);
  }

  public static IgniteQueue<MsgToClient> getOfflineMsgQueueOfSession(Ignite ignite, String sessionId, JsonObject config) {
    CollectionConfiguration colCfg = new CollectionConfiguration();
    colCfg.setCollocated(true);
    colCfg.setBackups(1);
    return ignite.queue(IgniteAssist.OFFLINE_MSG_QUEUE_PREFIX + sessionId, Config.getSessionQueuedMessageMax(config), colCfg);
  }

  public static IgniteCache<String, Retain> initRetainCache(Ignite ignite) {
    CacheConfiguration<String, Retain> retainCacheConfiguration = new CacheConfiguration<>();
    retainCacheConfiguration.setName(IgniteAssist.RETAIN_CACHE_NAME);
    retainCacheConfiguration.setWriteSynchronizationMode(CacheWriteSynchronizationMode.PRIMARY_SYNC);
    retainCacheConfiguration.setCacheMode(CacheMode.PARTITIONED);
    retainCacheConfiguration.setBackups(1);
    retainCacheConfiguration.setAtomicityMode(CacheAtomicityMode.ATOMIC);
    return ignite.getOrCreateCache(retainCacheConfiguration);
  }

  public static IgniteCache<String, Session> initSessionCache(Ignite ignite) {
    CacheConfiguration<String, Session> sessionCacheConfiguration = new CacheConfiguration<>();
    sessionCacheConfiguration.setName(IgniteAssist.SESSION_CACHE_NAME);
    sessionCacheConfiguration.setWriteSynchronizationMode(CacheWriteSynchronizationMode.PRIMARY_SYNC);
    sessionCacheConfiguration.setCacheMode(CacheMode.PARTITIONED);
    sessionCacheConfiguration.setBackups(1);
    sessionCacheConfiguration.setAtomicityMode(CacheAtomicityMode.ATOMIC);
    return ignite.getOrCreateCache(sessionCacheConfiguration);
  }

  public static IgniteCache<SubscriptionKey, Subscription> initExactSubscriptionCache(Ignite ignite, JsonObject config) {
    CacheConfiguration<SubscriptionKey, Subscription> exactSubscriptionCacheConfiguration = new CacheConfiguration<>();
    exactSubscriptionCacheConfiguration.setName(IgniteAssist.EXACT_SUBSCRIPTION_CACHE_NAME);
    exactSubscriptionCacheConfiguration.setWriteSynchronizationMode(CacheWriteSynchronizationMode.PRIMARY_SYNC);
    exactSubscriptionCacheConfiguration.setCacheMode(CacheMode.PARTITIONED);
    exactSubscriptionCacheConfiguration.setAtomicityMode(CacheAtomicityMode.ATOMIC);
    exactSubscriptionCacheConfiguration.setBackups(Config.getSubIgniteBackups(config));
    return ignite.getOrCreateCache(exactSubscriptionCacheConfiguration);
  }

  public static IgniteCache<SubscriptionKey, Subscription> initWildcardSubscriptionCache(Ignite ignite, JsonObject config) {
    CacheConfiguration<SubscriptionKey, Subscription> wildcardSubscriptionCacheConfiguration = new CacheConfiguration<>();
    wildcardSubscriptionCacheConfiguration.setName(IgniteAssist.WILDCARD_SUBSCRIPTION_CACHE_NAME);
    wildcardSubscriptionCacheConfiguration.setWriteSynchronizationMode(CacheWriteSynchronizationMode.PRIMARY_SYNC);
    wildcardSubscriptionCacheConfiguration.setCacheMode(CacheMode.PARTITIONED);
    wildcardSubscriptionCacheConfiguration.setAtomicityMode(CacheAtomicityMode.ATOMIC);
    wildcardSubscriptionCacheConfiguration.setBackups(Config.getSubIgniteBackups(config));
    return ignite.getOrCreateCache(wildcardSubscriptionCacheConfiguration);
  }

  public static IgniteCache<String, Will> initWillCache(Ignite ignite) {
    CacheConfiguration<String, Will> willCacheConfiguration = new CacheConfiguration<>();
    willCacheConfiguration.setName(IgniteAssist.WILL_CACHE_NAME);
    willCacheConfiguration.setWriteSynchronizationMode(CacheWriteSynchronizationMode.PRIMARY_SYNC);
    willCacheConfiguration.setCacheMode(CacheMode.PARTITIONED);
    willCacheConfiguration.setBackups(1);
    willCacheConfiguration.setAtomicityMode(CacheAtomicityMode.ATOMIC);
    return ignite.getOrCreateCache(willCacheConfiguration);
  }

}
