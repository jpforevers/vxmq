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

import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import io.vertx.mutiny.core.Vertx;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.extension.ExtendWith;

import java.nio.charset.StandardCharsets;
import java.util.function.BiConsumer;

@ExtendWith(VertxExtension.class)
public class BaseTest {

  private static VxmqLauncher vxmqLauncher;

  @BeforeAll
  static void startServer(Vertx vertx, VertxTestContext testContext) throws Throwable {
    if (vxmqLauncher == null) {
      vxmqLauncher = new VxmqLauncher();
      vxmqLauncher.start().subscribe().with(v -> testContext.completeNow(), testContext::failNow);
    } else {
      testContext.completeNow();
    }
  }

  protected <T> BiConsumer<? super T, ? super Throwable> whenCompleteBiConsumer(VertxTestContext testContext) {
    return (v, throwable) -> {
      if (throwable == null) {
        testContext.completeNow();
      } else {
        testContext.failNow(throwable);
      }
    };
  }

  public static byte[] encodeRemainingLength(int length) {
    int digit;
    StringBuilder encoded = new StringBuilder();
    do {
      digit = length % 128;
      length /= 128;
      if (length > 0) {
        digit |= 0x80; // Set the continuation bit
      }
      encoded.append((char) digit);
    } while (length > 0);
    return encoded.toString().getBytes();
  }

  // encode a string to MQTT UTF-8 encoded string
  protected byte[] encodeToMqttUtf8EncodedStringBytes(String value) {
    byte[] utf8Bytes = value.getBytes(StandardCharsets.UTF_8);
    byte[] encoded = new byte[2 + utf8Bytes.length];
    encoded[0] = (byte) (utf8Bytes.length >> 8); // MSB
    encoded[1] = (byte) (utf8Bytes.length & 0xFF); // LSB
    System.arraycopy(utf8Bytes, 0, encoded, 2, utf8Bytes.length);
    return encoded;
  }

  // encode password data to MQTT password field bytes
  protected byte[] encodeToMqttPasswordBytes(byte[] password) {
    byte[] encoded = new byte[2 + password.length];
    encoded[0] = (byte) (password.length >> 8); // MSB
    encoded[1] = (byte) (password.length & 0xFF); // LSB
    System.arraycopy(password, 0, encoded, 2, password.length);
    return encoded;
  }

}
