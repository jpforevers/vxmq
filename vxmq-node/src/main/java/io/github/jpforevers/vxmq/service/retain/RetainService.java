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

package io.github.jpforevers.vxmq.service.retain;

import io.smallrye.mutiny.Uni;

import java.util.List;

public interface RetainService {

  Uni<Void> saveOrUpdateRetain(Retain retain);

  Uni<Void> removeRetain(String topicName);

  Uni<List<Retain>> allTopicMatchRetains(String topicFilter);

  Uni<List<Retain>> allRetains();

  Uni<Long> count();

}
