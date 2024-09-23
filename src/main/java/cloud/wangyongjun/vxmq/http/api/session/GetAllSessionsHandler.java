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

package cloud.wangyongjun.vxmq.http.api.session;

import cloud.wangyongjun.vxmq.http.api.AbstractApiJsonResultHandler;
import cloud.wangyongjun.vxmq.service.session.SessionService;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.core.Vertx;
import io.vertx.mutiny.ext.web.RoutingContext;

public class GetAllSessionsHandler extends AbstractApiJsonResultHandler {

  private final SessionService sessionService;

  public GetAllSessionsHandler(Vertx vertx, SessionService sessionService) {
    super(vertx);
    this.sessionService = sessionService;
  }

  @Override
  public Uni<Object> computeJsonResult(RoutingContext routingContext) {

    return sessionService.allSessions().onItem().transform(sessions -> sessions);
  }

}
