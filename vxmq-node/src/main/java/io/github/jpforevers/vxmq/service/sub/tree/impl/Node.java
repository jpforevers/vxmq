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

package io.github.jpforevers.vxmq.service.sub.tree.impl;

import io.github.jpforevers.vxmq.service.sub.Subscription;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

class Node {

  private final String token;
  // token -> TreeNode
  private final Map<String, Node> children = new HashMap<>();
  // sessionId -> Subscription
  private final Map<String, Subscription> subscriptions = new HashMap<>();

  public Node(String token) {
    this.token = token;
  }

  public String getToken() {
    return token;
  }

  public Node getChild(String token) {
    return children.get(token);
  }

  public void addChild(Node treeNode) {
    children.put(treeNode.getToken(), treeNode);
  }

  public boolean childrenContains(String token) {
    return children.containsKey(token);
  }

  public void saveOrUpdateSubscription(Subscription subscription) {
    subscriptions.put(subscription.getSessionId(), subscription);
  }

  public void removeSubscription(String sessionId) {
    subscriptions.remove(sessionId);
  }

  public Collection<Subscription> getSubscriptions() {
    return subscriptions.values();
  }

  void DFSTraversalPreOrder(Consumer<Node> nodeConsumer) {
    nodeConsumer.accept(this);
    children.values().forEach(child -> child.DFSTraversalPreOrder(nodeConsumer));
  }

}
