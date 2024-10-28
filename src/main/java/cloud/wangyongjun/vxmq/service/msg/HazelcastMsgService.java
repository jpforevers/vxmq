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

package cloud.wangyongjun.vxmq.service.msg;

import cloud.wangyongjun.vxmq.assist.HazelcastAssist;
import cloud.wangyongjun.vxmq.assist.HazelcastUtil;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.core.Vertx;

import java.util.ArrayList;
import java.util.List;

public class HazelcastMsgService implements MsgService {

  private static volatile HazelcastMsgService hazelcastMsgService;

  public static HazelcastMsgService getSingleton(Vertx vertx) {
    if (hazelcastMsgService == null) {
      synchronized (HazelcastMsgService.class) {
        if (hazelcastMsgService == null) {
          hazelcastMsgService = new HazelcastMsgService(vertx);
        }
      }
    }
    return hazelcastMsgService;
  }

  private final Vertx vertx;
  private final HazelcastInstance hazelcastInstance;
  private final IMap<InboundQos2PubKey, InboundQos2Pub> inboundQos2PubCache;
  private final IMap<OutboundQos1PubKey, OutboundQos1Pub> outboundQos1PubCache;
  private final IMap<OutboundQos2PubKey, OutboundQos2Pub> outboundQos2PubCache;
  private final IMap<OutboundQos2RelKey, OutboundQos2Rel> outboundQos2RelCache;

  private HazelcastMsgService(Vertx vertx) {
    this.vertx = vertx;
    this.hazelcastInstance = HazelcastUtil.getHazelcastInstance(vertx);
    this.inboundQos2PubCache = HazelcastAssist.initInboundQos2PubCache(hazelcastInstance);
    this.outboundQos1PubCache = HazelcastAssist.initOutboundQos1PubCache(hazelcastInstance);
    this.outboundQos2PubCache = HazelcastAssist.initOutboundQos2PubCache(hazelcastInstance);
    this.outboundQos2RelCache = HazelcastAssist.initOutboundQos2RelCache(hazelcastInstance);
  }

  @Override
  public Uni<Void> saveInboundQos2Pub(InboundQos2Pub inboundQos2Pub) {
    inboundQos2PubCache.put(inboundQos2Pub.getKey(), inboundQos2Pub);
    return Uni.createFrom().voidItem();
  }

  @Override
  public Uni<InboundQos2Pub> getAndRemoveInboundQos2Pub(String sessionId, int messageId) {
    InboundQos2PubKey inboundQos2PubKey = new InboundQos2PubKey(sessionId, messageId);
    return Uni.createFrom().item(inboundQos2PubCache.remove(inboundQos2PubKey));
  }

  @Override
  public Uni<List<InboundQos2Pub>> allInboundQos2Pub() {
    return Uni.createFrom().item(inboundQos2PubCache.values().stream().toList());
  }

  @Override
  public Uni<Long> countInboundQos2Pub() {
    return Uni.createFrom().item((long) inboundQos2PubCache.size());
  }

  @Override
  public Uni<Void> saveOutboundQos1Pub(OutboundQos1Pub outboundQos1Pub) {
    outboundQos1PubCache.put(outboundQos1Pub.getKey(), outboundQos1Pub);
    return Uni.createFrom().voidItem();
  }

  @Override
  public Uni<OutboundQos1Pub> getAndRemoveOutboundQos1Pub(String sessionId, int messageId) {
    return Uni.createFrom().item(outboundQos1PubCache.remove(new OutboundQos1PubKey(sessionId, messageId)));
  }

  @Override
  public Uni<List<OutboundQos1Pub>> allOutboundQos1Pub() {
    return Uni.createFrom().item(outboundQos1PubCache.values().stream().toList());
  }

  @Override
  public Uni<List<OutboundQos1Pub>> outboundQos1Pub(String sessionId) {
    return Uni.createFrom().item(outboundQos1PubCache.values(mapEntry -> mapEntry.getKey().getSessionId().equals(sessionId)).stream().toList());
  }

  @Override
  public Uni<Long> countOutboundQos1Pub() {
    return Uni.createFrom().item((long) outboundQos1PubCache.size());
  }

  @Override
  public Uni<Void> saveOutboundQos2Pub(OutboundQos2Pub outboundQos2Pub) {
    outboundQos2PubCache.put(outboundQos2Pub.getKey(), outboundQos2Pub);
    return Uni.createFrom().voidItem();
  }

  @Override
  public Uni<OutboundQos2Pub> getAndRemoveOutboundQos2Pub(String sessionId, int messageId) {
    return Uni.createFrom().item(outboundQos2PubCache.remove(new OutboundQos2PubKey(sessionId, messageId)));
  }

  @Override
  public Uni<List<OutboundQos2Pub>> allOutboundQos2Pub() {
    return Uni.createFrom().item(outboundQos2PubCache.values().stream().toList());
  }

  @Override
  public Uni<List<OutboundQos2Pub>> outboundQos2Pub(String sessionId) {
    return Uni.createFrom().item(outboundQos2PubCache.values(mapEntry -> mapEntry.getKey().getSessionId().equals(sessionId)).stream().toList());
  }

  @Override
  public Uni<Long> countOutboundQos2Pub() {
    return Uni.createFrom().item((long) outboundQos2PubCache.size());
  }

  @Override
  public Uni<Void> saveOutboundQos2Rel(OutboundQos2Rel outboundQos2Rel) {
    outboundQos2RelCache.put(outboundQos2Rel.getKey(), outboundQos2Rel);
    return Uni.createFrom().voidItem();
  }

  @Override
  public Uni<OutboundQos2Rel> getAndRemoveOutboundQos2Rel(String sessionId, int messageId) {
    return Uni.createFrom().item(outboundQos2RelCache.remove(new OutboundQos2RelKey(sessionId, messageId)));
  }

  @Override
  public Uni<List<OutboundQos2Rel>> allOutboundQos2Rel() {
    return Uni.createFrom().item(outboundQos2RelCache.values().stream().toList());
  }

  @Override
  public Uni<List<OutboundQos2Rel>> outboundQos2Rel(String sessionId) {
    return Uni.createFrom().item(outboundQos2RelCache.values(mapEntry -> mapEntry.getKey().getSessionId().equals(sessionId)).stream().toList());
  }

  @Override
  public Uni<Long> countOutboundQos2Rel() {
    return Uni.createFrom().item((long) outboundQos2RelCache.size());
  }

  @Override
  public Uni<Void> saveOfflineMsg(MsgToClient msgToClient) {
    HazelcastAssist.getOfflineMsgQueueOfSession(hazelcastInstance, msgToClient.getSessionId()).add(msgToClient);
    return Uni.createFrom().voidItem();
  }

  @Override
  public Uni<List<MsgToClient>> allOfflineMsgOfSession(String sessionId) {
    return Uni.createFrom().item(new ArrayList<>(HazelcastAssist.getOfflineMsgQueueOfSession(hazelcastInstance, sessionId)));
  }

  @Override
  public Uni<Void> clearMsgs(String sessionId) {
    return vertx.executeBlocking(() -> {
      inboundQos2PubCache.removeAll(mapEntry -> mapEntry.getKey().getSessionId().equals(sessionId));
      outboundQos1PubCache.removeAll(mapEntry -> mapEntry.getKey().getSessionId().equals(sessionId));
      outboundQos2PubCache.removeAll(mapEntry -> mapEntry.getKey().getSessionId().equals(sessionId));
      outboundQos2RelCache.removeAll(mapEntry -> mapEntry.getKey().getSessionId().equals(sessionId));
      HazelcastAssist.getOfflineMsgQueueOfSession(hazelcastInstance, sessionId).destroy();
      return null;
    }, false);
  }

}
