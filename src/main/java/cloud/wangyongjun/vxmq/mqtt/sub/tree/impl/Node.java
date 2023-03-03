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
