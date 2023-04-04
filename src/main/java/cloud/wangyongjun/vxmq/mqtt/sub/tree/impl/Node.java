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

package cloud.wangyongjun.vxmq.mqtt.sub.tree.impl;

import cloud.wangyongjun.vxmq.mqtt.sub.Subscription;

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
