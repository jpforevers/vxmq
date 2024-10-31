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
 *
 */

package io.github.jpforevers.vxmq.init;

import io.github.jpforevers.vxmq.init.ebcodec.ToClientVerticleMsgCodec;
import io.github.jpforevers.vxmq.service.client.ToClientVerticleMsg;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.vertx.core.AbstractVerticle;
import io.vertx.mutiny.core.eventbus.EventBus;

public class InitVerticle extends AbstractVerticle {

  @Override
  public Uni<Void> asyncStart() {
    EventBus eventBus = vertx.eventBus();
    eventBus.getDelegate().registerDefaultCodec(ToClientVerticleMsg.class, new ToClientVerticleMsgCodec());
    return Uni.createFrom().voidItem();
  }

  @Override
  public Uni<Void> asyncStop() {
    return Uni.createFrom().voidItem();
  }

}
