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

package cloud.wangyongjun.vxmq.service.session;

import cloud.wangyongjun.vxmq.assist.IgniteAssist;
import cloud.wangyongjun.vxmq.assist.ModelConstants;
import cloud.wangyongjun.vxmq.assist.IgniteUtil;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.core.Vertx;
import org.apache.ignite.IgniteCache;
import org.apache.ignite.binary.BinaryObject;
import org.apache.ignite.binary.BinaryObjectBuilder;
import org.apache.ignite.cache.CacheEntryProcessor;
import org.apache.ignite.cache.query.QueryCursor;
import org.apache.ignite.cache.query.ScanQuery;

import javax.cache.Cache;
import javax.cache.processor.EntryProcessorException;
import javax.cache.processor.MutableEntry;
import java.util.List;
import java.util.stream.Collectors;

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
  public Uni<Void> saveOrUpdateSession(Session session) {
    sessionCache.put(session.getClientId(), session);
    return Uni.createFrom().voidItem();
  }

  @Override
  public Uni<Void> updateLatestUpdatedTime(String clientId, long time) {
    sessionCache.<String, BinaryObject>withKeepBinary()
        .invoke(clientId, new CacheEntryProcessor<String, BinaryObject, Object>() {
          @Override
          public Object process(MutableEntry<String, BinaryObject> entry, Object... arguments) throws EntryProcessorException {
            BinaryObjectBuilder binaryObjectBuilder = entry.getValue().toBuilder();
            binaryObjectBuilder.setField(ModelConstants.FIELD_NAME_UPDATED_TIME, time);
            entry.setValue(binaryObjectBuilder.build());
            return null;
          }
        });

    // 将上面的匿名类写法改成下面的lambda写法会报错，可能和java 17有关
    // java.lang.UnsupportedOperationException: can't get field offset on a hidden class: private final long cloud.wangyongjun.vxmq.service.session.IgniteSessionService$$Lambda$1745/0x0000000801254fb0.arg$1
//    sessionCache.
//      <String, BinaryObject>withKeepBinary()
//      .invoke(clientId, (CacheEntryProcessor<String, BinaryObject, Object>) (entry, arguments) -> {
//        BinaryObjectBuilder binaryObjectBuilder = entry.getValue().toBuilder();
//        binaryObjectBuilder.setField(ModelConstants.FIELD_UPDATED_TIME, time);
//        entry.setValue(binaryObjectBuilder.build());
//        return null;
//      });
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
    return Uni.createFrom().item(cursor.getAll().stream().map(Cache.Entry::getValue).collect(Collectors.toList()));
  }

  @Override
  public Uni<Long> count() {
    QueryCursor<String> cursor = sessionCache.<Cache.Entry<String, Session>, String>query(new ScanQuery<>(), Cache.Entry::getKey);
    return Uni.createFrom().item((long) cursor.getAll().size());
  }

}
