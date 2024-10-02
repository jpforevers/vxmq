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

package cloud.wangyongjun.vxmq;

import io.vertx.mutiny.core.buffer.Buffer;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class Main {

  public static void main(String[] args) {
    f1();
    f2();

  }

  private static void f1() {
    byte[] utf8Bytes = "value".getBytes(StandardCharsets.UTF_8);
    byte[] encoded = new byte[2 + utf8Bytes.length];
    encoded[0] = (byte) (utf8Bytes.length >> 8); // MSB
    encoded[1] = (byte) (utf8Bytes.length & 0xFF); // LSB
    System.arraycopy(utf8Bytes, 0, encoded, 2, utf8Bytes.length);
    System.out.println(Arrays.toString(encoded));
  }

  private static void f2() {
    Buffer buffer = Buffer.buffer();
    byte[] utf8Bytes = "value".getBytes(StandardCharsets.UTF_8);
    short msb = (short) (utf8Bytes.length >> 8); // MSB
    buffer.appendUnsignedByte(msb);
    short lsb = (short) (utf8Bytes.length & 0xFF); // LSB
    buffer.appendUnsignedByte(lsb);
    buffer.appendBytes(utf8Bytes);
    System.out.println(Arrays.toString(buffer.getBytes()));
  }

}
