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
import org.apache.ignite.IgniteCache;
import org.apache.ignite.cache.query.QueryCursor;
import org.apache.ignite.cache.query.ScanQuery;

import javax.cache.Cache;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class IgniteMsgService implements MsgService {

  private static volatile IgniteMsgService igniteMsgService;

  public static IgniteMsgService getSingleton(Vertx vertx) {
    if (igniteMsgService == null) {
      synchronized (IgniteMsgService.class) {
        if (igniteMsgService == null) {
          igniteMsgService = new IgniteMsgService(vertx);
        }
      }
    }
    return igniteMsgService;
  }

  private final Vertx vertx;
  private final Ignite ignite;
  private final IgniteCache<InboundQos2PubKey, InboundQos2Pub> inboundQos2PubCache;
  private final IgniteCache<OutboundQos1PubKey, OutboundQos1Pub> outboundQos1PubCache;
  private final IgniteCache<OutboundQos2PubKey, OutboundQos2Pub> outboundQos2PubCache;
  private final IgniteCache<OutboundQos2RelKey, OutboundQos2Rel> outboundQos2RelCache;

  private IgniteMsgService(Vertx vertx) {
    this.vertx = vertx;
    this.ignite = IgniteUtil.getIgnite(vertx);
    this.inboundQos2PubCache = IgniteAssist.initInboundQos2PubCache(ignite);
    this.outboundQos1PubCache = IgniteAssist.initOutboundQos1PubCache(ignite);
    this.outboundQos2PubCache = IgniteAssist.initOutboundQos2PubCache(ignite);
    this.outboundQos2RelCache = IgniteAssist.initOutboundQos2RelCache(ignite);
  }

  @Override
  public Uni<Void> saveInboundQos2Pub(InboundQos2Pub inboundQos2Pub) {
    inboundQos2PubCache.put(inboundQos2Pub.getKey(), inboundQos2Pub);
    return Uni.createFrom().voidItem();
  }

  @Override
  public Uni<InboundQos2Pub> getAndRemoveInboundQos2Pub(String sessionId, int messageId) {
    InboundQos2PubKey inboundQos2PubKey = new InboundQos2PubKey(sessionId, messageId);
    return Uni.createFrom().item(inboundQos2PubCache.getAndRemove(inboundQos2PubKey));
  }

  @Override
  public Uni<List<InboundQos2Pub>> allInboundQos2Pub() {
    QueryCursor<Cache.Entry<InboundQos2PubKey, InboundQos2Pub>> cursor = inboundQos2PubCache.query(new ScanQuery<>());
    return Uni.createFrom().item(cursor.getAll().stream().map(Cache.Entry::getValue).collect(Collectors.toList()));
  }

  @Override
  public Uni<Long> countInboundQos2Pub() {
    QueryCursor<InboundQos2PubKey> cursor = inboundQos2PubCache.<Cache.Entry<InboundQos2PubKey, InboundQos2Pub>, InboundQos2PubKey>query(new ScanQuery<>(), Cache.Entry::getKey);
    return Uni.createFrom().item((long) cursor.getAll().size());
  }

  @Override
  public Uni<Void> saveOutboundQos1Pub(OutboundQos1Pub outboundQos1Pub) {
    outboundQos1PubCache.put(outboundQos1Pub.getKey(), outboundQos1Pub);
    return Uni.createFrom().voidItem();
  }

  @Override
  public Uni<OutboundQos1Pub> getAndRemoveOutboundQos1Pub(String sessionId, int messageId) {
    return Uni.createFrom().item(outboundQos1PubCache.getAndRemove(new OutboundQos1PubKey(sessionId, messageId)));
  }

  @Override
  public Uni<List<OutboundQos1Pub>> allOutboundQos1Pub() {
    QueryCursor<Cache.Entry<OutboundQos1PubKey, OutboundQos1Pub>> cursor = outboundQos1PubCache.query(new ScanQuery<>());
    return Uni.createFrom().item(cursor.getAll().stream().map(Cache.Entry::getValue).collect(Collectors.toList()));
  }

  @Override
  public Uni<List<OutboundQos1Pub>> outboundQos1Pub(String sessionId) {
    QueryCursor<Cache.Entry<OutboundQos1PubKey, OutboundQos1Pub>> cursor = outboundQos1PubCache.query(new ScanQuery<>((key, value) -> key.getSessionId().equals(sessionId)));
    return Uni.createFrom().item(cursor.getAll().stream().map(Cache.Entry::getValue).collect(Collectors.toList()));
  }

  @Override
  public Uni<Long> countOutboundQos1Pub() {
    QueryCursor<OutboundQos1PubKey> cursor = outboundQos1PubCache.<Cache.Entry<OutboundQos1PubKey, OutboundQos1Pub>, OutboundQos1PubKey>query(new ScanQuery<>(), Cache.Entry::getKey);
    return Uni.createFrom().item((long) cursor.getAll().size());
  }

  @Override
  public Uni<Void> saveOutboundQos2Pub(OutboundQos2Pub outboundQos2Pub) {
    outboundQos2PubCache.put(outboundQos2Pub.getKey(), outboundQos2Pub);
    return Uni.createFrom().voidItem();
  }

  @Override
  public Uni<OutboundQos2Pub> getAndRemoveOutboundQos2Pub(String sessionId, int messageId) {
    return Uni.createFrom().item(outboundQos2PubCache.getAndRemove(new OutboundQos2PubKey(sessionId, messageId)));
  }

  @Override
  public Uni<List<OutboundQos2Pub>> allOutboundQos2Pub() {
    QueryCursor<Cache.Entry<OutboundQos2PubKey, OutboundQos2Pub>> cursor = outboundQos2PubCache.query(new ScanQuery<>());
    return Uni.createFrom().item(cursor.getAll().stream().map(Cache.Entry::getValue).collect(Collectors.toList()));
  }

  @Override
  public Uni<List<OutboundQos2Pub>> outboundQos2Pub(String sessionId) {
    QueryCursor<Cache.Entry<OutboundQos2PubKey, OutboundQos2Pub>> cursor = outboundQos2PubCache.query(new ScanQuery<>((key, value) -> key.getSessionId().equals(sessionId)));
    return Uni.createFrom().item(cursor.getAll().stream().map(Cache.Entry::getValue).collect(Collectors.toList()));
  }

  @Override
  public Uni<Long> countOutboundQos2Pub() {
    QueryCursor<OutboundQos2PubKey> cursor = outboundQos2PubCache.<Cache.Entry<OutboundQos2PubKey, OutboundQos2Pub>, OutboundQos2PubKey>query(new ScanQuery<>(), Cache.Entry::getKey);
    return Uni.createFrom().item((long) cursor.getAll().size());
  }

  @Override
  public Uni<Void> saveOutboundQos2Rel(OutboundQos2Rel outboundQos2Rel) {
    outboundQos2RelCache.put(outboundQos2Rel.getKey(), outboundQos2Rel);
    return Uni.createFrom().voidItem();
  }

  @Override
  public Uni<OutboundQos2Rel> getAndRemoveOutboundQos2Rel(String sessionId, int messageId) {
    return Uni.createFrom().item(outboundQos2RelCache.getAndRemove(new OutboundQos2RelKey(sessionId, messageId)));
  }

  @Override
  public Uni<List<OutboundQos2Rel>> allOutboundQos2Rel() {
    QueryCursor<Cache.Entry<OutboundQos2RelKey, OutboundQos2Rel>> cursor = outboundQos2RelCache.query(new ScanQuery<>());
    return Uni.createFrom().item(cursor.getAll().stream().map(Cache.Entry::getValue).collect(Collectors.toList()));
  }

  @Override
  public Uni<List<OutboundQos2Rel>> outboundQos2Rel(String sessionId) {
    QueryCursor<Cache.Entry<OutboundQos2RelKey, OutboundQos2Rel>> cursor = outboundQos2RelCache.query(new ScanQuery<>((key, value) -> key.getSessionId().equals(sessionId)));
    return Uni.createFrom().item(cursor.getAll().stream().map(Cache.Entry::getValue).collect(Collectors.toList()));
  }

  @Override
  public Uni<Long> countOutboundQos2Rel() {
    QueryCursor<OutboundQos2RelKey> cursor = outboundQos2RelCache.<Cache.Entry<OutboundQos2RelKey, OutboundQos2Rel>, OutboundQos2RelKey>query(new ScanQuery<>(), Cache.Entry::getKey);
    return Uni.createFrom().item((long) cursor.getAll().size());
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
      try (QueryCursor<InboundQos2PubKey> inboundQos2PubKeysCursor = inboundQos2PubCache.<Cache.Entry<InboundQos2PubKey, InboundQos2Pub>, InboundQos2PubKey>query(new ScanQuery<>((key, value) -> key.getSessionId().equals(sessionId)), Cache.Entry::getKey)) {
        for (InboundQos2PubKey inboundQos2PubKey : inboundQos2PubKeysCursor) {
          inboundQos2PubCache.remove(inboundQos2PubKey);
        }
      }

      try (QueryCursor<OutboundQos1PubKey> outboundQos1PubKeysCursor = outboundQos1PubCache.<Cache.Entry<OutboundQos1PubKey, OutboundQos1Pub>, OutboundQos1PubKey>query(new ScanQuery<>((key, value) -> key.getSessionId().equals(sessionId)), Cache.Entry::getKey)) {
        for (OutboundQos1PubKey outboundQos1PubKey : outboundQos1PubKeysCursor) {
          outboundQos1PubCache.remove(outboundQos1PubKey);
        }
      }

      try (QueryCursor<OutboundQos2PubKey> outboundQos2PubKeysCursor = outboundQos2PubCache.<Cache.Entry<OutboundQos2PubKey, OutboundQos2Pub>, OutboundQos2PubKey>query(new ScanQuery<>((key, value) -> key.getSessionId().equals(sessionId)), Cache.Entry::getKey)) {
        for (OutboundQos2PubKey outboundQos2PubKey : outboundQos2PubKeysCursor) {
          outboundQos2PubCache.remove(outboundQos2PubKey);
        }
      }

      try (QueryCursor<OutboundQos2RelKey> outboundQos2RelKeysCursor = outboundQos2RelCache.<Cache.Entry<OutboundQos2RelKey, OutboundQos2Rel>, OutboundQos2RelKey>query(new ScanQuery<>((key, value) -> key.getSessionId().equals(sessionId)), Cache.Entry::getKey)) {
        for (OutboundQos2RelKey outboundQos2RelKey : outboundQos2RelKeysCursor) {
          outboundQos2RelCache.remove(outboundQos2RelKey);
        }
      }

      IgniteAssist.getOfflineMsgQueueOfSession(ignite, sessionId).close();
      return null;
    }, false);
  }

}
