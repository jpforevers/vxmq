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

package cloud.wangyongjun.vxmq.service.retain;

import cloud.wangyongjun.vxmq.assist.IgniteAssist;
import cloud.wangyongjun.vxmq.assist.TopicUtil;
import cloud.wangyongjun.vxmq.assist.IgniteUtil;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.core.Vertx;
import org.apache.ignite.IgniteCache;
import org.apache.ignite.cache.query.QueryCursor;
import org.apache.ignite.cache.query.ScanQuery;

import javax.cache.Cache;
import java.util.List;
import java.util.stream.Collectors;

public class IgniteRetainService implements RetainService {

  private static volatile IgniteRetainService igniteRetainService;

  public static IgniteRetainService getSingleton(Vertx vertx) {
    if (igniteRetainService == null) {
      synchronized (IgniteRetainService.class) {
        if (igniteRetainService == null) {
          igniteRetainService = new IgniteRetainService(vertx);
        }
      }
    }
    return igniteRetainService;
  }

  private final IgniteCache<String, Retain> retainCache;

  private IgniteRetainService(Vertx vertx) {
    this.retainCache = IgniteAssist.initRetainCache(IgniteUtil.getIgnite(vertx));
  }

  @Override
  public Uni<Void> saveOrUpdateRetain(Retain retain) {
    retainCache.put(retain.getTopicName(), retain);
    return Uni.createFrom().voidItem();
  }

  @Override
  public Uni<Void> removeRetain(String topicName) {
    retainCache.remove(topicName);
    return Uni.createFrom().voidItem();
  }

  @Override
  public Uni<List<Retain>> allTopicMatchRetains(String topicFilter) {
    QueryCursor<Cache.Entry<String, Retain>> cursor = retainCache.query(new ScanQuery<>((key, value) -> TopicUtil.matches(topicFilter, key)));
    return Uni.createFrom().item(cursor.getAll().stream().map(Cache.Entry::getValue).collect(Collectors.toList()));
  }

  @Override
  public Uni<List<Retain>> allRetains() {
    QueryCursor<Cache.Entry<String, Retain>> cursor = retainCache.query(new ScanQuery<>());
    return Uni.createFrom().item(cursor.getAll().stream().map(Cache.Entry::getValue).collect(Collectors.toList()));
  }

  @Override
  public Uni<Long> count() {
    QueryCursor<String> cursor = retainCache.<Cache.Entry<String, Retain>, String>query(new ScanQuery<>(), Cache.Entry::getKey);
    return Uni.createFrom().item((long) cursor.getAll().size());
  }

}
