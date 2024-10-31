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

import cloud.wangyongjun.vxmq.service.HazelcastAssist;
import cloud.wangyongjun.vxmq.service.HazelcastUtil;
import cloud.wangyongjun.vxmq.assist.TopicUtil;
import com.hazelcast.map.IMap;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.core.Vertx;

import java.util.List;

public class HazelcastRetainService implements RetainService {

  private static volatile HazelcastRetainService hazelcastRetainService;

  public static HazelcastRetainService getSingleton(Vertx vertx) {
    if (hazelcastRetainService == null) {
      synchronized (HazelcastRetainService.class) {
        if (hazelcastRetainService == null) {
          hazelcastRetainService = new HazelcastRetainService(vertx);
        }
      }
    }
    return hazelcastRetainService;
  }

  private final IMap<String, Retain> retainCache;

  private HazelcastRetainService(Vertx vertx) {
    this.retainCache = HazelcastAssist.initRetainCache(HazelcastUtil.getHazelcastInstance(vertx));
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
    return Uni.createFrom().item(retainCache.values(mapEntry -> TopicUtil.matches(topicFilter, mapEntry.getKey())).stream().toList());
  }

  @Override
  public Uni<List<Retain>> allRetains() {
    return Uni.createFrom().item(retainCache.values().stream().toList());
  }

  @Override
  public Uni<Long> count() {
    return Uni.createFrom().item((long) retainCache.size());
  }

}
