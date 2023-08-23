/*
 * Copyright 2018-present 王用军
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package cloud.wangyongjun.vxmq.service.msg;

import cloud.wangyongjun.vxmq.assist.IgniteAssist;
import cloud.wangyongjun.vxmq.assist.IgniteUtil;
import io.smallrye.mutiny.Uni;
import io.vertx.core.json.JsonObject;
import io.vertx.mutiny.core.Vertx;
import org.apache.ignite.Ignite;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class IgniteAndMapMsgService implements MsgService {

  private static volatile IgniteAndMapMsgService igniteAndMapMsgService;

  public static IgniteAndMapMsgService getSingleton(Vertx vertx, JsonObject config) {
    if (igniteAndMapMsgService == null) {
      synchronized (IgniteAndMapMsgService.class) {
        if (igniteAndMapMsgService == null) {
          igniteAndMapMsgService = new IgniteAndMapMsgService(vertx, config);
        }
      }
    }
    return igniteAndMapMsgService;
  }

  private final Vertx vertx;
  private final Ignite ignite;
  private final JsonObject config;
  private final Map<InboundQos2PubKey, InboundQos2Pub> inboundQos2PubCache;
  private final Map<OutboundQos1PubKey, OutboundQos1Pub> outboundQos1PubCache;
  private final Map<OutboundQos2PubKey, OutboundQos2Pub> outboundQos2PubCache;
  private final Map<OutboundQos2RelKey, OutboundQos2Rel> outboundQos2RelCache;

  private IgniteAndMapMsgService(Vertx vertx, JsonObject config) {
    this.vertx = vertx;
    this.ignite = IgniteUtil.getIgnite(vertx);
    this.config = config;
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
    List<OutboundQos1Pub> result = new ArrayList<>();
    for (OutboundQos1PubKey outboundQos1PubKey : outboundQos1PubCache.keySet()) {
      if (outboundQos1PubKey.getSessionId().equals(sessionId)) {
        result.add(outboundQos1PubCache.get(outboundQos1PubKey));
      }
    }
    return Uni.createFrom().item(result);
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
    List<OutboundQos2Pub> result = new ArrayList<>();
    for (OutboundQos2PubKey outboundQos2PubKey : outboundQos2PubCache.keySet()) {
      if (outboundQos2PubKey.getSessionId().equals(sessionId)) {
        result.add(outboundQos2PubCache.get(outboundQos2PubKey));
      }
    }
    return Uni.createFrom().item(result);
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
    List<OutboundQos2Rel> result = new ArrayList<>();
    for (OutboundQos2RelKey outboundQos2RelKey : outboundQos2RelCache.keySet()) {
      if (outboundQos2RelKey.getSessionId().equals(sessionId)) {
        result.add(outboundQos2RelCache.get(outboundQos2RelKey));
      }
    }
    return Uni.createFrom().item(result);
  }

  @Override
  public Uni<Long> countOutboundQos2Rel() {
    return Uni.createFrom().item((long) outboundQos2RelCache.size());
  }

  @Override
  public Uni<Void> saveOfflineMsg(MsgToClient msgToClient) {
    IgniteAssist.getOfflineMsgQueueOfSession(ignite, msgToClient.getSessionId(), config).add(msgToClient);
    return Uni.createFrom().voidItem();
  }

  @Override
  public Uni<List<MsgToClient>> allOfflineMsgOfSession(String sessionId) {
    return Uni.createFrom().item(new ArrayList<>(IgniteAssist.getOfflineMsgQueueOfSession(ignite, sessionId, config)));
  }

  @Override
  public Uni<Void> clearMsgs(String sessionId) {
    return vertx.executeBlocking(Uni.createFrom().emitter(uniEmitter -> {
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

      IgniteAssist.getOfflineMsgQueueOfSession(ignite, sessionId, config).close();
      uniEmitter.complete(null);
    }), false);
  }

}
