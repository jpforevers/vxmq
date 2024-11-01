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

package io.github.jpforevers.vxmq.init.ebcodec;

import io.github.jpforevers.vxmq.service.client.ToClientVerticleMsg;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.MessageCodec;
import io.vertx.core.json.JsonObject;

public class ToClientVerticleMsgCodec implements MessageCodec<ToClientVerticleMsg, ToClientVerticleMsg> {

  @Override
  public void encodeToWire(Buffer buffer, ToClientVerticleMsg toClientVerticleMsg) {
    Buffer encoded = toClientVerticleMsg.toJson().toBuffer();
    buffer.appendInt(encoded.length());
    buffer.appendBuffer(encoded);
  }

  @Override
  public ToClientVerticleMsg decodeFromWire(int pos, Buffer buffer) {
    int length = buffer.getInt(pos);
    pos += 4;
    return new ToClientVerticleMsg(new JsonObject(buffer.slice(pos, pos + length)));
  }

  @Override
  public ToClientVerticleMsg transform(ToClientVerticleMsg toClientVerticleMsg) {
    return toClientVerticleMsg;
  }

  @Override
  public String name() {
    return ToClientVerticleMsgCodec.class.getSimpleName();
  }

  @Override
  public byte systemCodecID() {
    return -1;
  }

}
