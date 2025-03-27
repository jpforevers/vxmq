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

package io.github.jpforevers.vxmq;

import io.github.jpforevers.vxmq.service.sub.Subscription;

import java.time.Instant;
import java.util.List;

public class TestConstants {

  public static final List<Subscription> SUBSCRIPTIONS = List.of(
    new Subscription().setSessionId("s1").setClientId("c1").setTopicFilter("abc/def/123").setQos(1).setCreatedTime(Instant.now().toEpochMilli()),
    new Subscription().setSessionId("s2").setClientId("c2").setTopicFilter("+/def/123").setQos(1).setCreatedTime(Instant.now().toEpochMilli()),
    new Subscription().setSessionId("s3").setClientId("c3").setTopicFilter("abc/+/123").setQos(1).setCreatedTime(Instant.now().toEpochMilli()),
    new Subscription().setSessionId("s4").setClientId("c4").setTopicFilter("abc/def/+").setQos(1).setCreatedTime(Instant.now().toEpochMilli()),
    new Subscription().setSessionId("s5").setClientId("c5").setTopicFilter("#").setQos(1).setCreatedTime(Instant.now().toEpochMilli()),
    new Subscription().setSessionId("s6").setClientId("c6").setTopicFilter("abc/#").setQos(2).setCreatedTime(Instant.now().toEpochMilli()),
    new Subscription().setSessionId("s7").setClientId("c7").setTopicFilter("abc/def/#").setQos(2).setCreatedTime(Instant.now().toEpochMilli()),
    new Subscription().setSessionId("s8").setClientId("c8").setTopicFilter("abc/def/123/#").setQos(0).setCreatedTime(Instant.now().toEpochMilli()),
    new Subscription().setSessionId("s9").setClientId("c9").setTopicFilter("+/+/123").setQos(1).setCreatedTime(Instant.now().toEpochMilli()),
    new Subscription().setSessionId("s10").setClientId("c10").setTopicFilter("abc/+/+").setQos(1).setCreatedTime(Instant.now().toEpochMilli()),
    new Subscription().setSessionId("s11").setClientId("c11").setTopicFilter("+/def/+").setQos(1).setCreatedTime(Instant.now().toEpochMilli()),
    new Subscription().setSessionId("s12").setClientId("c12").setTopicFilter("+/+/+").setQos(1).setCreatedTime(Instant.now().toEpochMilli())
  );

}
