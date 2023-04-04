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

package cloud.wangyongjun.vxmq.mqtt;

import cloud.wangyongjun.vxmq.mqtt.exception.InvalidTopicException;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.Math.min;

public class TopicUtil {

  private static final char[] SHARED_SUBSCRIPTION_CHAR_ARRAY = "$share".toCharArray();
  private static final int SHARED_SUBSCRIPTION_LENGTH = SHARED_SUBSCRIPTION_CHAR_ARRAY.length;
  private static final char TOPIC_LEVEL_SEPARATOR = '/';

  private static final int GROUP_INDEX = 2;
  private static final int TOPIC_INDEX = 3;

  public static final char MULTI_LEVEL_WILDCARD = '#';
  public static final char SINGLE_LEVEL_WILDCARD = '+';

  private static final Pattern SHARED_SUBSCRIPTION_PATTERN = Pattern.compile("\\$share(/(.*?)/(.*))");

  public static boolean isSingleLevelWildcardToken(String token) {
    return token.equals(String.valueOf(SINGLE_LEVEL_WILDCARD));
  }

  public static boolean isMultiLevelWildcardToken(String token) {
    return token.equals(String.valueOf(MULTI_LEVEL_WILDCARD));
  }

  public static String[] parseTokens(String topic) {
    return StringUtils.splitPreserveAllTokens(topic, String.valueOf(TOPIC_LEVEL_SEPARATOR));
  }

  public static boolean matches(String topicSubscription, String actualTopic) throws InvalidTopicException {

    if (StringUtils.containsAny(actualTopic, "#+")) {
      throw new InvalidTopicException();
    }
    final String subscription = StringUtils.stripEnd(topicSubscription, "/");

    String topic = actualTopic;

    if (actualTopic.length() > 1) {
      topic = StringUtils.stripEnd(actualTopic, "/");

    }
    if (StringUtils.containsNone(topicSubscription, "#+")) {

      return subscription.equals(topic);
    }
    if (actualTopic.startsWith("$") && !topicSubscription.startsWith("$")) {
      return false;
    }
    return matchesWildcards(subscription, topic);
  }

  private static boolean matchesWildcards(final String topicSubscription, final String actualTopic) {

    if (topicSubscription.contains("#")) {
      if (!StringUtils.endsWith(topicSubscription, "/#") && topicSubscription.length() > 1) {
        return false;
      }
    }

    final String[] subscription = parseTokens(topicSubscription);
    final String[] topic = parseTokens(actualTopic);

    final int smallest = min(subscription.length, topic.length);

    for (int i = 0; i < smallest; i++) {
      final String sub = subscription[i];
      final String t = topic[i];

      if (!sub.equals(t)) {
        if (sub.equals("#")) {
          return true;
        } else if (sub.equals("+")) {
          //Matches Topic Level wildcard, so we can just ignore

        } else {
          //Does not match a wildcard and is not equal to the topic token
          return false;
        }
      }
    }
    //If the length is equal or the subscription token with the number x+1 (where x is the topic length) is a wildcard,
    //everything is alright.
    return subscription.length == topic.length ||
      (subscription.length - topic.length == 1 && (subscription[subscription.length - 1].equals("#")));
  }

  /**
   * Check if a topic is a shared subscription topic.
   *
   * @param topic the topic to check
   * @return true if it is a shared subscription, else false.
   */
  public static boolean isSharedSubscriptionTopic(String topic) {
    //optimizing
    if (!topic.startsWith("$share/")) {
      return false;
    }
    final Matcher matcher = SHARED_SUBSCRIPTION_PATTERN.matcher(topic);
    return matcher.matches();
  }

  public static String getShareNameFromSharedSubTopicFilter(String topicFilter) {
    String subStringAfterFirstForwardSlash = StringUtils.substringAfter(topicFilter, TOPIC_LEVEL_SEPARATOR);
    return StringUtils.substringBefore(subStringAfterFirstForwardSlash, TOPIC_LEVEL_SEPARATOR);
  }

  public static String getRealTopicFromSharedSubTopicFilter(String topicFilter) {
    String subStringAfterFirstForwardSlash = StringUtils.substringAfter(topicFilter, TOPIC_LEVEL_SEPARATOR);
    return StringUtils.substringAfter(subStringAfterFirstForwardSlash, TOPIC_LEVEL_SEPARATOR);
  }

  /**
   * Checks if the topic is valid to publish to.
   * <p>
   * This checks for invalid
   * <p>
   * <ul>
   * <li>#</li>
   * <li>+</li>
   * <li>illegal UTF-8 chars</li>
   * </ul>
   *
   * @param topic the topic to check
   * @return <code>true</code> if the topic is valid, <code>false</code> otherwise
   */
  public static boolean isValidTopicToPublish(String topic) {

    if (topic.isEmpty()) {
      return false;
    }
    if (topic.contains("\u0000")) {
      return false;
    }
    //noinspection IndexOfReplaceableByContains
    return !(topic.indexOf("#") > -1 || topic.indexOf("+") > -1);
  }

  /**
   * Checks if the topic is valid to subscribe to.
   * <p>
   * This check for invalid
   * <p>
   * <ul>
   * <li># combinations</li>
   * <li>+ combination</li>
   * <li>illegal UTF-8 chars</li>
   * </ul>
   *
   * @param topic the topic to check
   * @return <code>true</code> if the topic is valid, <code>false</code> otherwise
   */
  public static boolean isValidToSubscribe(String topic) {

    if (topic.isEmpty()) {
      return false;
    }
    if (topic.contains("\u0000")) {
      return false;
    }
    //We're using charAt because otherwise the String backing char[]
    //needs to be copied. JMH Benchmarks showed that this is more performant

    char lastChar = topic.charAt(0);
    char currentChar;
    int sharedSubscriptionDelimiterCharCount = 0;
    final int length = topic.length();
    boolean isSharedSubscription = false;
    int sharedCounter = lastChar == SHARED_SUBSCRIPTION_CHAR_ARRAY[0] ? 1 : -1;

    for (int i = 1; i < length; i++) {
      currentChar = topic.charAt(i);

      // current char still matching $share ?
      if (i < SHARED_SUBSCRIPTION_LENGTH && currentChar == SHARED_SUBSCRIPTION_CHAR_ARRAY[i]) {
        sharedCounter++;
      }

      // finally, is it a shared subscription?
      if (i == SHARED_SUBSCRIPTION_LENGTH
        && sharedCounter == SHARED_SUBSCRIPTION_LENGTH
        && currentChar == TOPIC_LEVEL_SEPARATOR) {
        isSharedSubscription = true;
      }

      //Check the shared name
      if (isSharedSubscription && sharedSubscriptionDelimiterCharCount == 1) {
        if (currentChar == '+' || currentChar == '#') {
          //Shared name contains wildcard chars
          return false;
        }
        if (lastChar == TOPIC_LEVEL_SEPARATOR && currentChar == TOPIC_LEVEL_SEPARATOR) {
          //Check if the shared name is empty
          return false;
        }
      }

      // how many times did we see the sharedSubscriptionDelimiter?
      if (isSharedSubscription && currentChar == TOPIC_LEVEL_SEPARATOR) {
        sharedSubscriptionDelimiterCharCount++;
      }

      // If the last character is a # and is prepended with /, then it's a valid subscription
      if (i == length - 1 && currentChar == '#' && lastChar == '/') {
        return true;
      }

      //Check if something follows after the # sign
      if (lastChar == '#' || (currentChar == '#' && i == length - 1)) {
        return false;
      }

      //Let's check if the + sign is in the middle of a string
      if (currentChar == '+' && lastChar != '/') {

        if (sharedSubscriptionDelimiterCharCount != 2 || !isSharedSubscription || lastChar != TOPIC_LEVEL_SEPARATOR) {
          return false;
        }
      }
      //Let's check if the + sign is followed by a
      if (lastChar == '+' && currentChar != '/') {
        return false;
      }
      lastChar = currentChar;
    }

    // Is a shared subscription but the second delimiter (/) never came
    return !isSharedSubscription || sharedSubscriptionDelimiterCharCount >= 2;
  }

  /**
   * Checks if the topic starts with '$'.
   *
   * @param topic the topic to check
   * @return <code>true</code> if the topic starts with '$' <code>false</code> otherwise
   */
  public static boolean isDollarTopic(String topic) {
    return topic.startsWith("$");
  }

  /**
   * Check if a topic contains any wildcard character ('#','+').
   *
   * @param topic the topic to check
   * @return true if it contains a wildcard character, else false.
   */
  public static boolean containsWildcard(final String topic) {
    return (topic.indexOf(MULTI_LEVEL_WILDCARD) != -1) ||
      (topic.indexOf(SINGLE_LEVEL_WILDCARD) != -1);
  }

  public static void main(String[] args) {
    String topic = "abc/def/123";
    System.out.println(TopicUtil.matches("abc/def/123", topic));
    System.out.println(TopicUtil.matches("+/def/123", topic));
    System.out.println(TopicUtil.matches("abc/+/123", topic));
    System.out.println(TopicUtil.matches("abc/def/+", topic));
    System.out.println(TopicUtil.matches("#", topic));
    System.out.println(TopicUtil.matches("abc/#", topic));
    System.out.println(TopicUtil.matches("abc/def/#", topic));
    System.out.println(TopicUtil.matches("abc/def/123/#", topic));
    System.out.println(TopicUtil.matches("abc//sd", topic));

  }

}
