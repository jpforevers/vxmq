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

package cloud.wangyongjun.vxmq.http.api;

import cloud.wangyongjun.vxmq.assist.ConsumerUtil;
import io.vertx.mutiny.ext.web.RoutingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Consumer;

public class ApiFailureHandler implements Consumer<RoutingContext> {

  private static final Logger LOGGER = LoggerFactory.getLogger(ApiFailureHandler.class);

  @Override
  public void accept(RoutingContext routingContext) {
    Throwable throwable = routingContext.failure();
    LOGGER.error("Error occurred when handling api request", throwable);
    if (throwable instanceof ApiException apiException) {
      routingContext.response().setStatusCode(apiException.errorCode().getHttpStatus());
      routingContext.json(apiException.toJson()).subscribe().with(ConsumerUtil.nothingToDo(), ConsumerUtil.nothingToDo());
    } else {
      routingContext.response().setStatusCode(500);
      routingContext.json(new ApiException(ApiErrorCode.COMMON_INTERNAL_SERVER_ERROR, throwable.getMessage()).toJson()).subscribe().with(ConsumerUtil.nothingToDo(), ConsumerUtil.nothingToDo());
    }
  }

}
