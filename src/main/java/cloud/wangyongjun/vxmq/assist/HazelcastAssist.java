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

package cloud.wangyongjun.vxmq.assist;

import cloud.wangyongjun.vxmq.service.msg.*;
import cloud.wangyongjun.vxmq.service.retain.Retain;
import cloud.wangyongjun.vxmq.service.session.Session;
import cloud.wangyongjun.vxmq.service.sub.Subscription;
import cloud.wangyongjun.vxmq.service.sub.SubscriptionKey;
import cloud.wangyongjun.vxmq.service.will.Will;
import com.hazelcast.collection.IQueue;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;

public class HazelcastAssist {

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

  public static IMap<InboundQos2PubKey, InboundQos2Pub> initInboundQos2PubCache(HazelcastInstance hazelcastInstance) {

    return hazelcastInstance.getMap(INBOUND_QOS2_PUB_CACHE_NAME);
  }

  public static IMap<OutboundQos1PubKey, OutboundQos1Pub> initOutboundQos1PubCache(HazelcastInstance hazelcastInstance) {

    return hazelcastInstance.getMap(OUTBOUND_QOS1_PUB_CACHE_NAME);
  }

  public static IMap<OutboundQos2PubKey, OutboundQos2Pub> initOutboundQos2PubCache(HazelcastInstance hazelcastInstance) {

    return hazelcastInstance.getMap(OUTBOUND_QOS2_PUB_CACHE_NAME);
  }

  public static IMap<OutboundQos2RelKey, OutboundQos2Rel> initOutboundQos2RelCache(HazelcastInstance hazelcastInstance) {

    return hazelcastInstance.getMap(OUTBOUND_QOS2_REL_CACHE_NAME);
  }

  public static IQueue<MsgToClient> getOfflineMsgQueueOfSession(HazelcastInstance hazelcastInstance, String sessionId) {

    String name = HazelcastAssist.OFFLINE_MSG_QUEUE_PREFIX + sessionId;
    // TODO 实现setEmptyQueueTtl
//    hazelcastInstance.getConfig().addQueueConfig(new QueueConfig(name).setEmptyQueueTtl());
    return hazelcastInstance.getQueue(name);
  }

  public static IMap<String, Retain> initRetainCache(HazelcastInstance hazelcastInstance) {

    return hazelcastInstance.getMap(RETAIN_CACHE_NAME);
  }

  public static IMap<String, Session> initSessionCache(HazelcastInstance hazelcastInstance) {

    return hazelcastInstance.getMap(SESSION_CACHE_NAME);
  }

  public static IMap<SubscriptionKey, Subscription> initExactSubscriptionCache(HazelcastInstance hazelcastInstance) {

    return hazelcastInstance.getMap(EXACT_SUBSCRIPTION_CACHE_NAME);
  }

  public static IMap<SubscriptionKey, Subscription> initWildcardSubscriptionCache(HazelcastInstance hazelcastInstance) {

    return hazelcastInstance.getMap(WILDCARD_SUBSCRIPTION_CACHE_NAME);
  }

  public static IMap<String, Will> initWillCache(HazelcastInstance hazelcastInstance) {

    return hazelcastInstance.getMap(WILL_CACHE_NAME);
  }

}
