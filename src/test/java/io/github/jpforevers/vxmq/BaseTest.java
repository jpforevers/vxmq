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

package io.github.jpforevers.vxmq;

import io.github.jpforevers.vxmq.assist.Config;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import io.vertx.mutiny.core.Vertx;
import io.vertx.mutiny.core.buffer.Buffer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.extension.ExtendWith;

import java.nio.charset.StandardCharsets;
import java.util.function.BiConsumer;

@ExtendWith(VertxExtension.class)
public class BaseTest {

  private static VxmqLauncher vxmqLauncher;

  protected static final int OUTBOUND_RECEIVE_MAXIMUM = 10;
  static {
    System.setProperty(Config.KEY_VXMQ_MQTT_FLOW_CONTROL_OUTBOUND_RECEIVE_MAXIMUM, String.valueOf(OUTBOUND_RECEIVE_MAXIMUM));
  }

  @BeforeAll
  static void startServer(Vertx vertx, VertxTestContext testContext) throws Throwable {
    if (vxmqLauncher == null) {
      vxmqLauncher = new VxmqLauncher();
      vxmqLauncher.start().subscribe().with(v -> testContext.completeNow(), testContext::failNow);
    } else {
      testContext.completeNow();
    }
  }

  @AfterAll
  static void stopServer(Vertx vertx, VertxTestContext testContext) throws Throwable {
    if (vxmqLauncher != null) {
      vxmqLauncher.stop().subscribe().with(v -> testContext.completeNow(), testContext::failNow);
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

  public byte[] encodeVariableByteIntegerBytes(int length) {
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

  protected byte[] encodeToMqttTwoByteIntegerBytes(int i) {
    Buffer buffer = Buffer.buffer();
    short msb = (short) (i >> 8); // MSB
    buffer.appendUnsignedByte(msb);
    short lsb = (short) (i & 0xFF); // LSB
    buffer.appendUnsignedByte(lsb);
    return buffer.getBytes();
  }

  // encode a string to MQTT UTF-8 encoded string
  protected byte[] encodeToMqttUtf8EncodedStringBytes(String value) {
    Buffer buffer = Buffer.buffer();
    byte[] utf8Bytes = value.getBytes(StandardCharsets.UTF_8);
    short msb = (short) (utf8Bytes.length >> 8); // MSB
    buffer.appendUnsignedByte(msb);
    short lsb = (short) (utf8Bytes.length & 0xFF); // LSB
    buffer.appendUnsignedByte(lsb);
    buffer.appendBytes(utf8Bytes);
    return buffer.getBytes();
  }

  // encode password data to MQTT password field bytes
  protected byte[] encodeToMqttPasswordBytes(byte[] password) {
    Buffer buffer = Buffer.buffer();
    short msb = (short) (password.length >> 8); // MSB
    buffer.appendUnsignedByte(msb);
    short lsb = (short) (password.length & 0xFF); // LSB
    buffer.appendUnsignedByte(lsb);
    buffer.appendBytes(password);
    return buffer.getBytes();
  }

}
