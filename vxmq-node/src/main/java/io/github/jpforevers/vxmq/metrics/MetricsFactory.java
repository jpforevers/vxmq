package io.github.jpforevers.vxmq.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.MeterBinder;
import io.micrometer.core.instrument.binder.jvm.*;
import io.micrometer.core.instrument.binder.logging.LogbackMetrics;
import io.micrometer.core.instrument.binder.system.FileDescriptorMetrics;
import io.micrometer.core.instrument.binder.system.ProcessorMetrics;
import io.micrometer.core.instrument.binder.system.UptimeMetrics;
import io.vertx.mutiny.core.Vertx;

import java.util.ArrayList;
import java.util.List;

public class MetricsFactory {

  private static final List<MeterBinder> METRICS = new ArrayList<>();

  public static void init(Vertx vertx, MeterRegistry registry) {
    UptimeMetrics uptimeMetrics = new UptimeMetrics();
    uptimeMetrics.bindTo(registry);
    METRICS.add(uptimeMetrics);

    FileDescriptorMetrics fileDescriptorMetrics = new FileDescriptorMetrics();
    fileDescriptorMetrics.bindTo(registry);
    METRICS.add(fileDescriptorMetrics);

    LogbackMetrics logbackMetrics = new LogbackMetrics();
    logbackMetrics.bindTo(registry);
    METRICS.add(logbackMetrics);

    ClassLoaderMetrics classLoaderMetrics = new ClassLoaderMetrics();
    classLoaderMetrics.bindTo(registry);
    METRICS.add(classLoaderMetrics);

    JvmMemoryMetrics jvmMemoryMetrics = new JvmMemoryMetrics();
    jvmMemoryMetrics.bindTo(registry);
    METRICS.add(jvmMemoryMetrics);

    JvmGcMetrics jvmGcMetrics = new JvmGcMetrics();
    jvmGcMetrics.bindTo(registry);
    METRICS.add(jvmGcMetrics);

    ProcessorMetrics processorMetrics = new ProcessorMetrics();
    processorMetrics.bindTo(registry);
    METRICS.add(processorMetrics);

    JvmThreadMetrics jvmThreadMetrics = new JvmThreadMetrics();
    jvmThreadMetrics.bindTo(registry);
    METRICS.add(jvmThreadMetrics);

    JvmInfoMetrics jvmInfoMetrics = new JvmInfoMetrics();
    jvmInfoMetrics.bindTo(registry);
    METRICS.add(jvmInfoMetrics);

    JvmHeapPressureMetrics jvmHeapPressureMetrics = new JvmHeapPressureMetrics();
    jvmHeapPressureMetrics.bindTo(registry);
    METRICS.add(jvmHeapPressureMetrics);

    JvmCompilationMetrics jvmCompilationMetrics = new JvmCompilationMetrics();
    jvmCompilationMetrics.bindTo(registry);
    METRICS.add(jvmCompilationMetrics);

    MqttPublishMetrics mqttPublishMetrics = new MqttPublishMetrics();
    mqttPublishMetrics.bindTo(registry);
    METRICS.add(mqttPublishMetrics);

    MqttSessionMetrics mqttSessionMetrics = new MqttSessionMetrics(vertx);
    mqttSessionMetrics.bindTo(registry);
    METRICS.add(mqttSessionMetrics);

    MqttClientVerticleMetrics mqttClientVerticleMetrics = new MqttClientVerticleMetrics(vertx);
    mqttClientVerticleMetrics.bindTo(registry);
    METRICS.add(mqttClientVerticleMetrics);
  }

  public static void clean() {
    for (MeterBinder meterBinder : METRICS) {
      if (meterBinder instanceof AutoCloseable) {
        try {
          ((AutoCloseable) meterBinder).close();
        } catch (Exception e) {
          throw new RuntimeException("Error occurred when closing MeterBinder", e);
        }
      }
    }
  }

  /**
   * Get MQTT PUBLISH received counter
   * @return may be null if metrics not enable
   */
  public static Counter getMqttPublishReceivedCounter() {
    return METRICS.stream()
      .filter(meterBinder -> meterBinder instanceof MqttPublishMetrics)
      .map(meterBinder -> ((MqttPublishMetrics) meterBinder).getMqttPublishReceivedCounter())
      .findAny().orElse(null);
  }

  /**
   * Get MQTT PUBLISH sent counter
   * @return may be null if metrics not enable
   */
  public static Counter getMqttPublishSentCounter() {
    return METRICS.stream()
      .filter(meterBinder -> meterBinder instanceof MqttPublishMetrics)
      .map(meterBinder -> ((MqttPublishMetrics) meterBinder).getMqttPublishSentCounter())
      .findAny().orElse(null);
  }

}
