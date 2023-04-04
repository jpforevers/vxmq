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

package cloud.wangyongjun.vxmq.mqtt.retain;

import cloud.wangyongjun.vxmq.mqtt.IgniteAssist;
import cloud.wangyongjun.vxmq.mqtt.TopicUtil;
import cloud.wangyongjun.vxmq.mqtt.IgniteUtil;
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
