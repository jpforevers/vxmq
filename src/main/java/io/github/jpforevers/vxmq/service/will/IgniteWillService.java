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

package io.github.jpforevers.vxmq.service.will;

import io.github.jpforevers.vxmq.assist.IgniteAssist;
import io.github.jpforevers.vxmq.assist.IgniteUtil;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.core.Vertx;
import org.apache.ignite.IgniteCache;
import org.apache.ignite.cache.query.QueryCursor;
import org.apache.ignite.cache.query.ScanQuery;

import javax.cache.Cache;
import java.util.List;
import java.util.stream.Collectors;

public class IgniteWillService implements WillService {

  private static volatile IgniteWillService igniteWillService;

  public static IgniteWillService getSingleton(Vertx vertx) {
    if (igniteWillService == null) {
      synchronized (IgniteWillService.class) {
        if (igniteWillService == null) {
          igniteWillService = new IgniteWillService(vertx);
        }
      }
    }
    return igniteWillService;
  }

  private final IgniteCache<String, Will> willCache;

  private IgniteWillService(Vertx vertx) {
    this.willCache = IgniteAssist.initWillCache(IgniteUtil.getIgnite(vertx));
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
