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

package cloud.wangyongjun.vxmq.http.api.session;

import cloud.wangyongjun.vxmq.assist.ModelConstants;
import cloud.wangyongjun.vxmq.http.api.AbstractApiJsonResultHandler;
import cloud.wangyongjun.vxmq.service.composite.CompositeService;
import io.smallrye.mutiny.Uni;
import io.vertx.core.json.JsonObject;
import io.vertx.mutiny.core.Vertx;
import io.vertx.mutiny.ext.web.RoutingContext;
import org.apache.commons.lang3.StringUtils;

public class DeleteSessionByClientIdHandler extends AbstractApiJsonResultHandler {

  private final CompositeService compositeService;

  public DeleteSessionByClientIdHandler(Vertx vertx, CompositeService compositeService) {
    super(vertx);
    this.compositeService = compositeService;
  }

  @Override
  public Uni<Object> computeJsonResult(RoutingContext routingContext) {
    String clientId = routingContext.pathParam(ModelConstants.FIELD_NAME_CLIENT_ID);
    return compositeService.deleteSession(clientId).replaceWith(new JsonObject());
  }

}
