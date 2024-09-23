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

package cloud.wangyongjun.vxmq.service.sub;

import cloud.wangyongjun.vxmq.assist.EBServices;
import cloud.wangyongjun.vxmq.service.sub.share.ShareSubscriptionProcessor;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.vertx.core.AbstractVerticle;
import io.vertx.serviceproxy.ServiceBinder;

// 部署Verticle时，如果实例数量为1，那么该Verticle是完全线程安全的，始终只有一个并发线程运行该Verticle。
// 我们利用该机制来实现订阅树的线程安全。
public class SubVerticle extends AbstractVerticle {

  @Override
  public Uni<Void> asyncStart() {
    SubService subService = IgniteAndSubTreeSubService.getInstance(vertx, ShareSubscriptionProcessor.getInstance(vertx));
    new ServiceBinder(vertx.getDelegate()).setAddress(EBServices.SUB_SERVICE.getEbAddress())
      .registerLocal(SubService.class, subService);
    return Uni.createFrom().voidItem();
  }

  @Override
  public Uni<Void> asyncStop() {
    return Uni.createFrom().voidItem();
  }

}
