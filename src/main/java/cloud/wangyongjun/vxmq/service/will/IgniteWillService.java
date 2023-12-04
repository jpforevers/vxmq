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

package cloud.wangyongjun.vxmq.service.will;

import cloud.wangyongjun.vxmq.assist.IgniteAssist;
import cloud.wangyongjun.vxmq.assist.IgniteUtil;
import io.smallrye.mutiny.Uni;
import io.vertx.core.json.JsonObject;
import io.vertx.mutiny.core.Vertx;
import org.apache.ignite.IgniteCache;
import org.apache.ignite.cache.query.QueryCursor;
import org.apache.ignite.cache.query.ScanQuery;

import javax.cache.Cache;
import java.util.List;
import java.util.stream.Collectors;

public class IgniteWillService implements WillService {

  private static volatile IgniteWillService igniteWillService;

  public static IgniteWillService getSingleton(Vertx vertx, JsonObject config) {
    if (igniteWillService == null) {
      synchronized (IgniteWillService.class) {
        if (igniteWillService == null) {
          igniteWillService = new IgniteWillService(vertx, config);
        }
      }
    }
    return igniteWillService;
  }

  private final IgniteCache<String, Will> willCache;

  private IgniteWillService(Vertx vertx, JsonObject config) {
    this.willCache = IgniteAssist.initWillCache(IgniteUtil.getIgnite(vertx), config);
  }

  @Override
  public Uni<Void> saveOrUpdateWill(Will will) {
    willCache.put(will.getSessionId(), will);
    return Uni.createFrom().voidItem();
  }

  @Override
  public Uni<Will> getWill(String sessionId) {
    return Uni.createFrom().item(willCache.get(sessionId));
  }

  @Override
  public Uni<Void> removeWill(String sessionId) {
    willCache.remove(sessionId);
    return Uni.createFrom().voidItem();
  }

  @Override
  public Uni<List<Will>> allWills() {
    QueryCursor<Cache.Entry<String, Will>> cursor = willCache.query(new ScanQuery<>());
    return Uni.createFrom().item(cursor.getAll().stream().map(Cache.Entry::getValue).collect(Collectors.toList()));
  }

  @Override
  public Uni<Long> count() {
    QueryCursor<String> cursor = willCache.<Cache.Entry<String, Will>, String>query(new ScanQuery<>(), Cache.Entry::getKey);
    return Uni.createFrom().item((long) cursor.getAll().size());
  }

}
