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

package cloud.wangyongjun.vxmq.service.sub;

import cloud.wangyongjun.vxmq.assist.EBServices;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.vertx.core.AbstractVerticle;
import io.vertx.serviceproxy.ServiceBinder;

// 部署Verticle时，如果实例数量为1，那么该Verticle是完全线程安全的，始终只有一个并发线程运行该Verticle。
// 我们利用该机制来实现订阅树的线程安全。
public class SubVerticle extends AbstractVerticle {

  @Override
  public Uni<Void> asyncStart() {
    SubService subService = IgniteAndSubTreeSubService.getInstance(vertx);
    new ServiceBinder(vertx.getDelegate()).setAddress(EBServices.SUB_SERVICE.getEbAddress())
      .registerLocal(SubService.class, subService);
    return Uni.createFrom().voidItem();
  }

  @Override
  public Uni<Void> asyncStop() {
    return Uni.createFrom().voidItem();
  }

}
