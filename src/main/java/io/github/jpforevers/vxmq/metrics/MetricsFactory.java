package io.github.jpforevers.vxmq.metrics;

import io.github.jpforevers.vxmq.assist.Config;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.MeterBinder;
import io.vertx.micrometer.backends.BackendRegistries;
import io.vertx.mutiny.core.Vertx;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.atomic.AtomicReference;

public class MetricsFactory {

  private static final AtomicReference<Counter> packetsPublishReceivedCounter = new AtomicReference<>();

  private static final AtomicReference<Counter> packetsPublishSentCounter = new AtomicReference<>();

  @Nullable
  public static Counter getPacketsPublishReceivedCounter() {
    if (Config.getMetricsEnable()) {
      // 使用 AtomicReference 的 compareAndSet 实现懒加载和线程安全
      if (packetsPublishReceivedCounter.get() == null) {
        MeterRegistry registry = BackendRegistries.getDefaultNow();
        Counter newCounter = Counter.builder("packets.publish.received")
          .description("Number of PUBLISH packets received")
          .register(registry);
        packetsPublishReceivedCounter.compareAndSet(null, newCounter);
      }
      return packetsPublishReceivedCounter.get();
    } else {
      return null;
    }
  }

  @Nullable
  public static Counter getPacketsPublishSentCounter() {
    if (Config.getMetricsEnable()) {
      // 使用 AtomicReference 的 compareAndSet 实现懒加载和线程安全
      if (packetsPublishSentCounter.get() == null) {
        MeterRegistry registry = BackendRegistries.getDefaultNow();
        Counter newCounter = Counter.builder("packets.publish.sent")
          .description("Number of PUBLISH packets sent")
          .register(registry);
        packetsPublishSentCounter.compareAndSet(null, newCounter);
      }
      return packetsPublishSentCounter.get();
    } else {
      return null;
    }
  }

  public static class PacketsPublishReceivedRateGaugeMetrics implements MeterBinder {

    private double ago;
    private double now;
    private double rate;

    public PacketsPublishReceivedRateGaugeMetrics(long computeRateIntervalSeconds, Vertx vertx, Counter packetsPublishReceivedCounter) {
      if (packetsPublishReceivedCounter != null) {
        vertx.setPeriodic(computeRateIntervalSeconds * 1000, l -> {
          this.now = packetsPublishReceivedCounter.count();
          if (this.ago == 0) {
            this.ago = packetsPublishReceivedCounter.count();
          } else {
            this.rate = (now - ago) / computeRateIntervalSeconds;
            this.ago = this.now;
          }
        });
      }
    }

    @Override
    public void bindTo(@NotNull MeterRegistry registry) {
      Gauge.builder("packets.publish.received.rate", this, PacketsPublishReceivedRateGaugeMetrics::getRate)
        .register(registry);
    }

    public double getRate() {
      return rate;
    }

  }

  public static class PacketsPublishSentRateGaugeMetrics implements MeterBinder {

    private double ago;
    private double now;
    private double rate;

    public PacketsPublishSentRateGaugeMetrics(long computeRateIntervalSeconds, Vertx vertx, Counter packetsPublishSentCounter) {
      if (packetsPublishSentCounter != null) {
        vertx.setPeriodic(computeRateIntervalSeconds * 1000, l -> {
          this.now = packetsPublishSentCounter.count();
          if (this.ago == 0) {
            this.ago = packetsPublishSentCounter.count();
          } else {
            this.rate = (now - ago) / computeRateIntervalSeconds;
            this.ago = this.now;
          }
        });
      }
    }

    @Override
    public void bindTo(@NotNull MeterRegistry registry) {
      Gauge.builder("packets.publish.sent.rate", this, PacketsPublishSentRateGaugeMetrics::getRate)
        .register(registry);
    }

    public double getRate() {
      return rate;
    }

  }

}
