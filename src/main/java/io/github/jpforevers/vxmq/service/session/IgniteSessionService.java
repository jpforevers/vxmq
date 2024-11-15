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

package io.github.jpforevers.vxmq.service.session;

import io.github.jpforevers.vxmq.assist.IgniteAssist;
import io.github.jpforevers.vxmq.assist.ModelConstants;
import io.github.jpforevers.vxmq.assist.IgniteUtil;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.core.Vertx;
import org.apache.ignite.IgniteCache;
import org.apache.ignite.binary.BinaryObject;
import org.apache.ignite.binary.BinaryObjectBuilder;
import org.apache.ignite.cache.CacheEntryProcessor;
import org.apache.ignite.cache.query.QueryCursor;
import org.apache.ignite.cache.query.ScanQuery;

import javax.cache.Cache;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class IgniteSessionService implements SessionService {

  private static volatile IgniteSessionService igniteSessionService;

  public static IgniteSessionService getSingleton(Vertx vertx) {
    if (igniteSessionService == null) {
      synchronized (IgniteSessionService.class) {
        if (igniteSessionService == null) {
          igniteSessionService = new IgniteSessionService(vertx);
        }
      }
    }
    return igniteSessionService;
  }

  private final IgniteCache<String, Session> sessionCache;

  private IgniteSessionService(Vertx vertx) {
    this.sessionCache = IgniteAssist.initSessionCache(IgniteUtil.getIgnite(vertx));
  }

  @Override
  public Uni<Session> getSession(String clientId) {
    return Uni.createFrom().item(sessionCache.get(clientId));
  }

  @Override
  public Uni<Map<Session.Field, Object>> getSessionByFields(String clientId, Session.Field[] fields) {
    BinaryObject binaryObject = sessionCache.<String, BinaryObject>withKeepBinary().get(clientId);
    if (binaryObject == null) {
      return Uni.createFrom().item(Map.of());
    } else {
      Map<Session.Field, Object> result = new HashMap<>(fields.length);
      for (Session.Field field : fields) {
        Object value = binaryObject.field(field.name());
        result.put(field, value);
      }
      return Uni.createFrom().item(result);
    }
  }

  @Override
  public Uni<Void> saveOrUpdateSession(Session session) {
    sessionCache.put(session.getClientId(), session);
    return Uni.createFrom().voidItem();
  }

  @Override
  public Uni<Void> updateSessionByFields(String clientId, Map<Session.Field, Object> fields) {
    sessionCache.<String, BinaryObject>withKeepBinary()
      .invoke(clientId, (CacheEntryProcessor<String, BinaryObject, Object>) (entry, arguments) -> {
        BinaryObjectBuilder binaryObjectBuilder = entry.getValue().toBuilder();
        for (Map.Entry<Session.Field, Object> fieldObjectEntry : fields.entrySet()) {
          Session.Field field = fieldObjectEntry.getKey();
          Object value = fieldObjectEntry.getValue();
          binaryObjectBuilder.setField(field.name(), value);
        }
        entry.setValue(binaryObjectBuilder.build());
        return null;
      });
    return Uni.createFrom().voidItem();
  }

  @Override
  public Uni<Void> updateLatestUpdatedTime(String clientId, long time) {
    sessionCache.<String, BinaryObject>withKeepBinary()
        .invoke(clientId, (CacheEntryProcessor<String, BinaryObject, Object>) (entry, arguments) -> {
          BinaryObjectBuilder binaryObjectBuilder = entry.getValue().toBuilder();
          binaryObjectBuilder.setField(ModelConstants.FIELD_NAME_UPDATED_TIME, time);
          entry.setValue(binaryObjectBuilder.build());
          return null;
        });
    return Uni.createFrom().voidItem();
  }

  @Override
  public Uni<Void> removeSession(String clientId) {
    sessionCache.remove(clientId);
    return Uni.createFrom().voidItem();
  }

  @Override
  public Uni<List<Session>> allSessions() {
    QueryCursor<Cache.Entry<String, Session>> cursor = sessionCache.query(new ScanQuery<>());
    return Uni.createFrom().item(cursor.getAll().stream().map(Cache.Entry::getValue).toList());
  }

  public Uni<List<Session>> search(String nodeId) {
    QueryCursor<Cache.Entry<String, Session>> cursor = sessionCache.query(new ScanQuery<>((k ,v) -> v.getNodeId().equals(nodeId)));
    return Uni.createFrom().item(cursor.getAll().stream().map(Cache.Entry::getValue).toList());
  }

  @Override
  public Uni<Session> getSessionByVerticleId(String verticleId) {
    QueryCursor<Cache.Entry<String, Session>> cursor = sessionCache.query(new ScanQuery<>((k ,v) -> v.getVerticleId().equals(verticleId)));
    return Uni.createFrom().item(cursor.getAll().stream().map(Cache.Entry::getValue).toList().get(0));
  }

  @Override
  public Uni<Long> count() {
    QueryCursor<String> cursor = sessionCache.<Cache.Entry<String, Session>, String>query(new ScanQuery<>(), Cache.Entry::getKey);
    return Uni.createFrom().item((long) cursor.getAll().size());
  }

}
