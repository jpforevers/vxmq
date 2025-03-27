package io.github.jpforevers.vxmq.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.MeterBinder;
import org.jetbrains.annotations.NotNull;

class MqttPublishMetrics implements MeterBinder {

  private Counter mqttPublishReceivedCounter;
  private Counter mqttPublishSentCounter;

  @Override
  public void bindTo(@NotNull MeterRegistry registry) {
    mqttPublishReceivedCounter = Counter.builder("mqtt.publish.received.num")
      .description("Number of MQTT PUBLISH packets received")
      .register(registry);
    mqttPublishSentCounter = Counter.builder("mqtt.publish.sent.num")
      .description("Number of MQTT PUBLISH packets sent")
      .register(registry);
  }

  public Counter getMqttPublishReceivedCounter() {
    return mqttPublishReceivedCounter;
  }

  public Counter getMqttPublishSentCounter() {
    return mqttPublishSentCounter;
  }

}
