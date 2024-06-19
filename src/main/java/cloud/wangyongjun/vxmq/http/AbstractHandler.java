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

package cloud.wangyongjun.vxmq.http;

import cloud.wangyongjun.vxmq.assist.ConsumerUtil;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.core.Vertx;
import io.vertx.mutiny.ext.web.RoutingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Consumer;

public abstract class AbstractHandler implements Consumer<RoutingContext> {

  protected final Logger logger = LoggerFactory.getLogger(this.getClass());

  protected final Vertx vertx;

  protected AbstractHandler(Vertx vertx) {
    this.vertx = vertx;
  }

  @Override
  public void accept(RoutingContext routingContext) {
    if (logger.isDebugEnabled()){
      logger.debug("{} request from {} to {}", routingContext.request().method(), routingContext.request().remoteAddress(), routingContext.request().uri());
    }
    handleRequest(routingContext).subscribe().with(ConsumerUtil.nothingToDo(), routingContext::fail);
  }

  public abstract Uni<Void> handleRequest(RoutingContext routingContext);

}
