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
import cloud.wangyongjun.vxmq.http.api.ApiErrorCode;
import cloud.wangyongjun.vxmq.http.api.ApiException;
import cloud.wangyongjun.vxmq.service.client.ClientService;
import cloud.wangyongjun.vxmq.service.composite.CompositeService;
import cloud.wangyongjun.vxmq.service.session.SessionService;
import io.smallrye.mutiny.Uni;
import io.vertx.core.json.JsonObject;
import io.vertx.mutiny.core.Vertx;
import io.vertx.mutiny.ext.web.RoutingContext;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DeleteSessionByClientIdHandler extends AbstractApiJsonResultHandler {

  private final static Logger LOGGER = LoggerFactory.getLogger(DeleteSessionByClientIdHandler.class);

  private final SessionService sessionService;
  private final ClientService clientService;
  private final CompositeService compositeService;

  public DeleteSessionByClientIdHandler(Vertx vertx, SessionService sessionService, ClientService clientService, CompositeService compositeService) {
    super(vertx);
    this.sessionService = sessionService;
    this.clientService = clientService;
    this.compositeService = compositeService;
  }

  @Override
  public Uni<Object> computeJsonResult(RoutingContext routingContext) {
    String clientId = routingContext.pathParam(ModelConstants.FIELD_NAME_CLIENT_ID);
    return Uni.createFrom().voidItem()
      .onItem().transformToUni(v -> sessionService.getSession(clientId))
      .onItem().transformToUni(session -> {
        if (session == null) {
          return Uni.createFrom().failure(new ApiException(ApiErrorCode.COMMON_NOT_FOUND, "Client not found: " + clientId));
        }else {
          if (session.isOnline() && StringUtils.isNotBlank(session.getVerticleId())){
            return clientService.closeMqttEndpoint(session.getVerticleId());
          }else {
            return Uni.createFrom().voidItem()
              .onItem().transformToUni(vv -> obtainClientLock(clientId))
              .onItem().transformToUni(vv -> compositeService.clearSession(clientId))
              .onItemOrFailure().call((vv, t) -> releaseClientLock(clientId));
          }
        }
      })
      .replaceWith(new JsonObject());
  }

  /**
   * Get client lock
   * @param clientId clientId
   * @return Void
   */
  public Uni<Void> obtainClientLock(String clientId){
    return clientService.obtainClientLock(clientId, 5000)
      .onItem().invoke(lock -> {
        if (LOGGER.isDebugEnabled()){
          LOGGER.debug("Client lock obtained for {}", clientId);
        }
      });
  }

  /**
   * Release client lock
   * @param clientId clientId
   * @return Void
   */
  public Uni<Void> releaseClientLock(String clientId){
    return clientService.releaseClientLock(clientId)
      .onItem().invoke(v -> {
        if (LOGGER.isDebugEnabled()){
          LOGGER.debug("Client lock released for {}", clientId);
        }
      });
  }

}
