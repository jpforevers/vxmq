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

import cloud.wangyongjun.vxmq.mqtt.TopicUtil;
import cloud.wangyongjun.vxmq.mqtt.sub.Subscription;
import cloud.wangyongjun.vxmq.mqtt.sub.tree.SubTree;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class SubTreeTrieAndRecursiveImpl implements SubTree {

  private final Node root = new Node(null);

  @Override
  public void saveOrUpdateSubscription(Subscription subscription) {
    String[] tokens = TopicUtil.parseTokens(subscription.getTopicFilter());
    Node currentNode = root;
    for (String token : tokens) {
      if (!currentNode.childrenContains(token)) {
        currentNode.addChild(new Node(token));
      }
      currentNode = currentNode.getChild(token);
    }
    currentNode.saveOrUpdateSubscription(subscription);
  }

  @Override
  public List<Subscription> findAllMatch(String topicName, boolean distinct) {
    String[] tokens = TopicUtil.parseTokens(topicName);
    List<Subscription> allMatch = new ArrayList<>();
    findAllMatchNodesRecursive(root, tokens, 0, node -> allMatch.addAll(node.getSubscriptions()));
    return distinct ? SubTree.distinct(allMatch) : allMatch;
  }

  /**
   * 递归寻找匹配的所有节点
   * @param node node
   * @param tokens tokens
   * @param tokenIndex tokenIndex
   * @param consumer consumer
   */
  private void findAllMatchNodesRecursive(Node node, String[] tokens, int tokenIndex, Consumer<Node> consumer) {
    if (node == null) {
      return;
    }
    if (tokenIndex == tokens.length) {
      consumer.accept(node);
      // “sport/#” also matches the singular “sport”, since # includes the parent level.
      if (node.childrenContains(String.valueOf(TopicUtil.MULTI_LEVEL_WILDCARD))) {
        consumer.accept(node.getChild(String.valueOf(TopicUtil.MULTI_LEVEL_WILDCARD)));
      }
      return;
    }
    String token = tokens[tokenIndex];
    if (node.childrenContains(token)) {
      // Exact match
      findAllMatchNodesRecursive(node.getChild(token), tokens, tokenIndex + 1, consumer);
    }
    if (node.childrenContains(String.valueOf(TopicUtil.SINGLE_LEVEL_WILDCARD))) {
      // Single level wildcard match
      findAllMatchNodesRecursive(node.getChild(String.valueOf(TopicUtil.SINGLE_LEVEL_WILDCARD)), tokens, tokenIndex + 1, consumer);
    }
    if (node.childrenContains(String.valueOf(TopicUtil.MULTI_LEVEL_WILDCARD))) {
      // Multi level wildcard match
      if (tokenIndex == tokens.length - 1) {
        findAllMatchNodesRecursive(node.getChild(String.valueOf(TopicUtil.MULTI_LEVEL_WILDCARD)), tokens, tokenIndex + 1, consumer);
      } else {
        for (int i = tokenIndex; i < tokens.length; i++) {
          findAllMatchNodesRecursive(node.getChild(String.valueOf(TopicUtil.MULTI_LEVEL_WILDCARD)), tokens, i + 1, consumer);
        }
      }
    }
  }

  /**
   * 递归寻找相等的那个节点
   * @param node node
   * @param tokens tokens
   * @param tokenIndex tokenIndex
   * @param consumer consumer
   */
  private void findTheEqualNodeRecursive(Node node, String[] tokens, int tokenIndex, Consumer<Node> consumer) {
    if (node == null) {
      return;
    }
    if (tokenIndex == tokens.length) {
      consumer.accept(node);
      return;
    }
    String token = tokens[tokenIndex];
    if (node.childrenContains(token)) {
      // Exact match
      findTheEqualNodeRecursive(node.getChild(token), tokens, tokenIndex + 1, consumer);
    }
  }

  @Override
  public List<Subscription> allSubs() {
    List<Subscription> subscriptions = new ArrayList<>();
    root.DFSTraversalPreOrder(node -> subscriptions.addAll(node.getSubscriptions()));
    return subscriptions;
  }

  @Override
  public void removeSubscription(String sessionId, String topicFilter) {
    findTheEqualNodeRecursive(root, TopicUtil.parseTokens(topicFilter), 0, node -> node.removeSubscription(sessionId));
  }

  @Override
  public void clearSubscription(String sessionId) {
    root.DFSTraversalPreOrder(node -> node.removeSubscription(sessionId));
  }

  public static void main(String[] args) {
    Subscription s1 = new Subscription().setSessionId("s1").setClientId("c1").setTopicFilter("abc/def/123").setQos(1).setCreatedTime(Instant.now().toEpochMilli());
    Subscription s2 = new Subscription().setSessionId("s2").setClientId("c2").setTopicFilter("+/def/123").setQos(1).setCreatedTime(Instant.now().toEpochMilli());
    Subscription s3 = new Subscription().setSessionId("s1").setClientId("c1").setTopicFilter("abc/+/123").setQos(1).setCreatedTime(Instant.now().toEpochMilli());
    Subscription s4 = new Subscription().setSessionId("s4").setClientId("c4").setTopicFilter("abc/def/+").setQos(1).setCreatedTime(Instant.now().toEpochMilli());
    Subscription s5 = new Subscription().setSessionId("s5").setClientId("c5").setTopicFilter("#").setQos(1).setCreatedTime(Instant.now().toEpochMilli());
    Subscription s6 = new Subscription().setSessionId("s6").setClientId("c6").setTopicFilter("abc/#").setQos(2).setCreatedTime(Instant.now().toEpochMilli());
    Subscription s7 = new Subscription().setSessionId("s7").setClientId("c7").setTopicFilter("abc/def/#").setQos(2).setCreatedTime(Instant.now().toEpochMilli());
    Subscription s8 = new Subscription().setSessionId("s8").setClientId("c8").setTopicFilter("abc/def/123/#").setQos(0).setCreatedTime(Instant.now().toEpochMilli());
    Subscription s9 = new Subscription().setSessionId("s9").setClientId("c9").setTopicFilter("+/+/123").setQos(1).setCreatedTime(Instant.now().toEpochMilli());
    Subscription s10 = new Subscription().setSessionId("s10").setClientId("c10").setTopicFilter("abc/+/+").setQos(1).setCreatedTime(Instant.now().toEpochMilli());
    Subscription s11 = new Subscription().setSessionId("s11").setClientId("c11").setTopicFilter("+/def/+").setQos(1).setCreatedTime(Instant.now().toEpochMilli());
    Subscription s12 = new Subscription().setSessionId("s12").setClientId("c12").setTopicFilter("+/+/+").setQos(1).setCreatedTime(Instant.now().toEpochMilli());


    Subscription f1 = new Subscription().setSessionId("f1").setClientId("f1").setTopicFilter("abc/def/456").setQos(1).setCreatedTime(Instant.now().toEpochMilli());
    Subscription f2 = new Subscription().setSessionId("f2").setClientId("f2").setTopicFilter("+").setQos(2).setCreatedTime(Instant.now().toEpochMilli());
    Subscription f3 = new Subscription().setSessionId("f3").setClientId("f3").setTopicFilter("abc/def/123/789").setQos(0).setCreatedTime(Instant.now().toEpochMilli());


    SubTree subTree = new SubTreeTrieAndRecursiveImpl();
    subTree.saveOrUpdateSubscription(s1);
    subTree.saveOrUpdateSubscription(s2);
    subTree.saveOrUpdateSubscription(s3);
    subTree.saveOrUpdateSubscription(s4);
    subTree.saveOrUpdateSubscription(s5);
    subTree.saveOrUpdateSubscription(s6);
    subTree.saveOrUpdateSubscription(s7);
    subTree.saveOrUpdateSubscription(s8);
    subTree.saveOrUpdateSubscription(s9);
    subTree.saveOrUpdateSubscription(s10);
    subTree.saveOrUpdateSubscription(s11);
    subTree.saveOrUpdateSubscription(s12);

    subTree.saveOrUpdateSubscription(f1);
    subTree.saveOrUpdateSubscription(f2);
    subTree.saveOrUpdateSubscription(f3);

    List<Subscription> allMatch = subTree.findAllMatch("abc/def/123", false);

    List<Subscription> subscriptions1 = subTree.allSubs();
    subTree.removeSubscription("s1", "abc/+/123");
    List<Subscription> subscriptions2 = subTree.allSubs();
    subTree.clearSubscription("s1");
    List<Subscription> subscriptions3 = subTree.allSubs();

    System.out.println(123);

  }

}
