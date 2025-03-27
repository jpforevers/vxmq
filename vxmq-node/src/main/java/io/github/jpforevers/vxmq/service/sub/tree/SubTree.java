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

package io.github.jpforevers.vxmq.service.sub.tree;

import io.github.jpforevers.vxmq.service.sub.Subscription;
import io.github.jpforevers.vxmq.service.sub.tree.impl.SubTreeTrieAndRecursiveImpl;

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
