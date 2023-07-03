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

package cloud.wangyongjun.vxmq.service.sub.tree;

import cloud.wangyongjun.vxmq.service.sub.Subscription;
import cloud.wangyongjun.vxmq.service.sub.tree.impl.SubTreeTrieAndRecursiveImpl;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public interface SubTree {

  static SubTree subTree() {
    return new SubTreeTrieAndRecursiveImpl();
  }

  void saveOrUpdateSubscription(Subscription subscription);

  List<Subscription> findAllMatch(String topicName, boolean distinct);

  List<Subscription> allSubs();

  void removeSubscription(String sessionId, String topicFilter);

  void clearSubscription(String sessionId);

  static List<Subscription> distinct(List<Subscription> subscriptions) {
    return new ArrayList<>(
      subscriptions.stream()
        .collect(Collectors.toMap(Subscription::getSessionId,
          Function.identity(),
          (s1, s2) -> s1.getQos() > s2.getQos() ? s1 : s2))
        .values()
    );
  }

}
