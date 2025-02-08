package io.github.jpforevers.vxmq.metrics;

import io.github.jpforevers.vxmq.service.ServiceFactory;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.MeterBinder;
import io.vertx.mutiny.core.Vertx;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;

public class MqttSessionMetrics implements MeterBinder {

  private final Vertx vertx;

  public MqttSessionMetrics(Vertx vertx) {
    this.vertx = vertx;
  }

  @Override
  public void bindTo(@NotNull MeterRegistry registry) {
    Gauge.builder("mqtt.session.num", () -> ServiceFactory.sessionService(vertx).allSessions().await().atMost(Duration.ofSeconds(5)).size())
      .description("Number of MQTT sessions")
      .register(registry);
  }

}
