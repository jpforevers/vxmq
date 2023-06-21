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
