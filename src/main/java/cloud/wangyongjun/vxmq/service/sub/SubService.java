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
