package io.github.jpforevers.vxmq.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.MeterBinder;
import org.jetbrains.annotations.NotNull;

class MqttPublishSentMetrics implements MeterBinder {

  private Counter counter;

  @Override
  public void bindTo(@NotNull MeterRegistry registry) {
    counter = Counter.builder("mqtt.publish.sent")
      .description("Number of MQTT PUBLISH packets sent")
      .register(registry);
  }

  public Counter getCounter() {
    return counter;
  }

}
