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

package io.github.jpforevers.vxmq.http.api.v2.session;

import java.util.Optional;

import io.github.jpforevers.vxmq.assist.ModelConstants;
import io.github.jpforevers.vxmq.http.api.AbstractApiJsonResultHandler;
import io.github.jpforevers.vxmq.http.api.CursorPagination;
import io.github.jpforevers.vxmq.service.session.SessionService;
import io.smallrye.mutiny.Uni;
import io.vertx.ext.web.validation.RequestParameters;
import io.vertx.json.schema.SchemaRouterOptions;
import io.vertx.json.schema.draft7.dsl.Schemas;
import io.vertx.mutiny.core.Vertx;
import io.vertx.mutiny.ext.web.RoutingContext;
import io.vertx.mutiny.ext.web.validation.ValidationHandler;
import io.vertx.mutiny.ext.web.validation.builder.Parameters;
import io.vertx.mutiny.ext.web.validation.builder.ValidationHandlerBuilder;
import io.vertx.mutiny.json.schema.SchemaParser;
import io.vertx.mutiny.json.schema.SchemaRouter;

public class GetSessionsHandler extends AbstractApiJsonResultHandler {

  private final SessionService sessionService;

  public GetSessionsHandler(Vertx vertx, SessionService sessionService) {
    super(vertx);
    this.sessionService = sessionService;
  }

  @Override
  public Uni<Object> computeJsonResult(RoutingContext routingContext) {
    RequestParameters params = routingContext.get(ValidationHandler.REQUEST_CONTEXT_KEY);
    Integer size = Optional.ofNullable(params.queryParameter(CursorPagination.FIELD_NAME_SIZE)).map(requestParameter -> requestParameter.getInteger()).orElse(null);
    String nextCursor = Optional.ofNullable(params.queryParameter(CursorPagination.FIELD_NAME_NEXT_CURSOR)).map(requestParameter -> requestParameter.getString()).orElse(null);
    String nodeId = Optional.ofNullable(params.queryParameter(ModelConstants.FIELD_NAME_NODE_ID)).map(requestParameter -> requestParameter.getString()).orElse(null);
    Boolean online = Optional.ofNullable(params.queryParameter(ModelConstants.FIELD_NAME_ONLINE)).map(requestParameter -> requestParameter.getBoolean()).orElse(null);
    Boolean cleanSession = Optional.ofNullable(params.queryParameter(ModelConstants.FIELD_NAME_CLEAN_SESSION)).map(requestParameter -> requestParameter.getBoolean()).orElse(null);

    return sessionService.search(size, nextCursor, nodeId, online, cleanSession).onItem().transform(sessions -> sessions);
  }

  public static ValidationHandler validationHandler(Vertx vertx) {
    // TODO When upgrading Vert.x to 5, the SchemaParser here should be migrated to the SchemaRepository, reference: https://github.com/vert-x3/vertx-examples/blob/5.x/web-examples/src/main/java/io/vertx/example/web/validation/ValidationExampleServer.java
    SchemaParser parser = SchemaParser.createDraft7SchemaParser(
      SchemaRouter.create(vertx, new SchemaRouterOptions())
    );
    
    return ValidationHandlerBuilder.create(parser)
      .queryParameter(Parameters.optionalParam(CursorPagination.FIELD_NAME_SIZE, Schemas.intSchema().nullable()))
      .queryParameter(Parameters.optionalParam(CursorPagination.FIELD_NAME_NEXT_CURSOR, Schemas.stringSchema().nullable()))
      .queryParameter(Parameters.optionalParam(ModelConstants.FIELD_NAME_NODE_ID, Schemas.stringSchema().nullable()))
      .queryParameter(Parameters.optionalParam(ModelConstants.FIELD_NAME_ONLINE, Schemas.booleanSchema().nullable()))
      .queryParameter(Parameters.optionalParam(ModelConstants.FIELD_NAME_CLEAN_SESSION, Schemas.booleanSchema().nullable()))
      .build();
  }

}
