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

package io.github.jpforevers.vxmq.service.session;

import io.github.jpforevers.vxmq.assist.Nullable;
import io.github.jpforevers.vxmq.http.api.SearchResult;
import io.smallrye.mutiny.Uni;

import java.util.List;
import java.util.Map;

public interface SessionService {

  @Nullable
  Uni<Session> getSession(String clientId);

  Uni<Map<Session.Field, Object>> getSessionByFields(String clientId, Session.Field[] fields);

  Uni<Void> saveOrUpdateSession(Session session);

  Uni<Void> updateSessionByFields(String clientId, Map<Session.Field, Object> fields);

  Uni<Void> updateLatestUpdatedTime(String clientId, long time);

  Uni<Void> removeSession(String clientId);

  Uni<List<Session>> allSessions();

  Uni<SearchResult<Session>> search(Integer size, String nextCursor, String nodeId, Boolean online, Boolean cleanSession);

  Uni<Session> getSessionByVerticleId(String verticleId);

  Uni<Long> count();

}
