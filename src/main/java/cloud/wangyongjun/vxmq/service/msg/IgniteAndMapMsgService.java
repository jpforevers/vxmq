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

import cloud.wangyongjun.vxmq.assist.IgniteAssist;
import cloud.wangyongjun.vxmq.assist.IgniteUtil;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.core.Vertx;
import org.apache.ignite.Ignite;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class IgniteAndMapMsgService implements MsgService {

  private static volatile IgniteAndMapMsgService igniteAndMapMsgService;

  public static IgniteAndMapMsgService getSingleton(Vertx vertx) {
    if (igniteAndMapMsgService == null) {
      synchronized (IgniteAndMapMsgService.class) {
        if (igniteAndMapMsgService == null) {
          igniteAndMapMsgService = new IgniteAndMapMsgService(vertx);
        }
      }
    }
    return igniteAndMapMsgService;
  }

  private final Vertx vertx;
  private final Ignite ignite;
  private final Map<InboundQos2PubKey, InboundQos2Pub> inboundQos2PubCache;
  private final Map<OutboundQos1PubKey, OutboundQos1Pub> outboundQos1PubCache;
  private final Map<OutboundQos2PubKey, OutboundQos2Pub> outboundQos2PubCache;
  private final Map<OutboundQos2RelKey, OutboundQos2Rel> outboundQos2RelCache;

  private IgniteAndMapMsgService(Vertx vertx) {
    this.vertx = vertx;
    this.ignite = IgniteUtil.getIgnite(vertx);
    this.inboundQos2PubCache = new ConcurrentHashMap<>();
    this.outboundQos1PubCache = new ConcurrentHashMap<>();
    this.outboundQos2PubCache = new ConcurrentHashMap<>();
    this.outboundQos2RelCache = new ConcurrentHashMap<>();
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
    return Uni.createFrom().item(outboundQos1PubCache.entrySet().stream().filter(entry -> entry.getKey().getSessionId().equals(sessionId)).map(Map.Entry::getValue).toList());
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
    return Uni.createFrom().item(outboundQos2PubCache.entrySet().stream().filter(entry -> entry.getKey().getSessionId().equals(sessionId)).map(Map.Entry::getValue).toList());
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
    return Uni.createFrom().item(outboundQos2RelCache.entrySet().stream().filter(entry -> entry.getKey().getSessionId().equals(sessionId)).map(Map.Entry::getValue).toList());
  }

  @Override
  public Uni<Long> countOutboundQos2Rel() {
    return Uni.createFrom().item((long) outboundQos2RelCache.size());
  }

  @Override
  public Uni<Void> saveOfflineMsg(MsgToClient msgToClient) {
    IgniteAssist.getOfflineMsgQueueOfSession(ignite, msgToClient.getSessionId()).add(msgToClient);
    return Uni.createFrom().voidItem();
  }

  @Override
  public Uni<List<MsgToClient>> allOfflineMsgOfSession(String sessionId) {
    return Uni.createFrom().item(new ArrayList<>(IgniteAssist.getOfflineMsgQueueOfSession(ignite, sessionId)));
  }

  @Override
  public Uni<Void> clearMsgs(String sessionId) {
    return vertx.executeBlocking(() -> {
      for (InboundQos2PubKey inboundQos2PubKey : inboundQos2PubCache.keySet()) {
        if (inboundQos2PubKey.getSessionId().equals(sessionId)) {
          inboundQos2PubCache.remove(inboundQos2PubKey);
        }
      }

      for (OutboundQos1PubKey outboundQos1PubKey : outboundQos1PubCache.keySet()) {
        if (outboundQos1PubKey.getSessionId().equals(sessionId)) {
          outboundQos1PubCache.remove(outboundQos1PubKey);
        }
      }

      for (OutboundQos2PubKey outboundQos2PubKey : outboundQos2PubCache.keySet()) {
        if (outboundQos2PubKey.getSessionId().equals(sessionId)) {
          outboundQos2PubCache.remove(outboundQos2PubKey);
        }
      }

      for (OutboundQos2RelKey outboundQos2RelKey : outboundQos2RelCache.keySet()) {
        if (outboundQos2RelKey.getSessionId().equals(sessionId)) {
          outboundQos2RelCache.remove(outboundQos2RelKey);
        }
      }

      IgniteAssist.getOfflineMsgQueueOfSession(ignite, sessionId).close();
      return null;
    }, false);
  }

}
