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

package cloud.wangyongjun.vxmq.service.sub;

import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.Future;

import java.util.List;

@ProxyGen
@VertxGen
public interface SubService {

  /**
   * Clear all subscriptions of the session.
   *
   * @param sessionId sessionId
   * @return Void
   */
  Future<Void> clearSubs(String sessionId);

  /**
   * Save or update subscriptions.
   *
   * @param subscription Subscription
   * @return If subscription already exist.
   */
  Future<Boolean> saveOrUpdateSub(Subscription subscription);

  /**
   * Remove subscription.
   *
   * @param sessionId   sessionId
   * @param topicFilter topicFilter
   * @return Whether the subscription to remove exist.
   */
  Future<Boolean> removeSub(String sessionId, String topicFilter);

  /**
   * All match subscriptions.
   *
   * @param topicName topicName
   * @param distinct  distinct
   * @return Subscriptions
   */
  Future<List<Subscription>> allMatchSubs(String topicName, boolean distinct);

  /**
   * All match exact subscriptions.
   *
   * @param topicName topicName
   * @return Subscriptions
   */
  Future<List<Subscription>> allMatchExactSubs(String topicName);

  /**
   * All match wildcard subscriptions.
   *
   * @param topicName topicName
   * @param distinct  distinct
   * @return Subscriptions
   */
  Future<List<Subscription>> allMatchWildcardSubs(String topicName, boolean distinct);

  /**
   * All exact subscriptions.
   *
   * @return Subscriptions
   */
  Future<List<Subscription>> allExactSubs();

  /**
   * All wildcard subscriptions.
   *
   * @return Subscriptions
   */
  Future<List<Subscription>> allWildcardSubs();

  /**
   * All subscriptions.
   *
   * @return Subscriptions
   */
  Future<List<Subscription>> allSubs();

  /**
   * Count match exact subscriptions.
   *
   * @param topicName topicName
   * @return Amount
   */
  Future<Long> countMatchExactSubs(String topicName);

  /**
   * Count match wildcard subscriptions.
   *
   * @param topicName topicName
   * @param distinct  distinct
   * @return Amount
   */
  Future<Long> countMatchWildcardSubs(String topicName, boolean distinct);

  /**
   * Count match subscriptions.
   *
   * @param topicName topicName
   * @param distinct  distinct
   * @return Amount
   */
  Future<Long> countMatch(String topicName, boolean distinct);

  /**
   * Count exact subscriptions.
   *
   * @return Amount
   */
  Future<Long> countExactSubs();

  /**
   * Count wildcard subscriptions.
   *
   * @return Amount
   */
  Future<Long> countWildcardSubs();

  /**
   * Count subscriptions.
   *
   * @return Amount
   */
  Future<Long> count();

}
