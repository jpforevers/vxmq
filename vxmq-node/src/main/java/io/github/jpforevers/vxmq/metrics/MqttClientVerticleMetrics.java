package io.github.jpforevers.vxmq.metrics;

import io.github.jpforevers.vxmq.service.ServiceFactory;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.MeterBinder;
import io.vertx.mutiny.core.Vertx;
import org.jetbrains.annotations.NotNull;

public class MqttClientVerticleMetrics implements MeterBinder {

  private final Vertx vertx;

  public MqttClientVerticleMetrics(Vertx vertx) {
    this.vertx = vertx;
  }

  @Override
  public void bindTo(@NotNull MeterRegistry registry) {
    Gauge.builder("mqtt.verticle.num", () -> ServiceFactory.clientService(vertx).verticleIds().size())
      .description("Number of MQTT client verticle")
      .register(registry);
  }

}
