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
import io.github.jpforevers.vxmq.assist.ConsumerUtil;
import io.github.jpforevers.vxmq.metrics.MetricsFactory;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.system.FileDescriptorMetrics;
import io.micrometer.core.instrument.binder.system.UptimeMetrics;
import io.smallrye.mutiny.Uni;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.VertxOptions;
import io.vertx.core.eventbus.EventBusOptions;
import io.vertx.core.spi.cluster.ClusterManager;
import io.vertx.micrometer.MicrometerMetricsOptions;
import io.vertx.micrometer.VertxPrometheusOptions;
import io.vertx.micrometer.backends.BackendRegistries;
import io.vertx.mutiny.core.Vertx;
import io.vertx.spi.cluster.ignite.IgniteClusterManager;
import org.apache.commons.lang3.StringUtils;
import org.apache.ignite.configuration.IgniteConfiguration;
import org.apache.ignite.logger.slf4j.Slf4jLogger;
import org.apache.ignite.spi.discovery.tcp.TcpDiscoverySpi;
import org.apache.ignite.spi.discovery.tcp.ipfinder.multicast.TcpDiscoveryMulticastIpFinder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.Arrays;

public class VxmqLauncher {

  static {
    System.setProperty("java.net.preferIPv4Stack", "true");
  }

  private static final Logger LOGGER = LoggerFactory.getLogger(VxmqLauncher.class);

  private Vertx vertx;
  private String mainVerticleId;

  public static void main(String[] args) {
    VxmqLauncher vxmqLauncher = new VxmqLauncher();
    Uni.createFrom().voidItem()
      .onItem().transformToUni(v -> vxmqLauncher.start())
      .subscribe().with(ConsumerUtil.nothingToDo(), ConsumerUtil.nothingToDo());
  }

  public Uni<Void> start() {
    Instant start = Instant.now();
    return Uni.createFrom().voidItem()
      .onItem().transformToUni(v -> initVertx())
      .onItem().transformToUni(v -> vertx.deployVerticle(MainVerticle.class.getName(), new DeploymentOptions()))
      .onItem().invoke(id -> this.mainVerticleId = id)
      .replaceWithVoid()
      .onItem().invoke(v -> LOGGER.info("VXMQ started in {} ms", Instant.now().toEpochMilli() - start.toEpochMilli()))
      .onFailure().invoke(t -> LOGGER.error("Error occurred when starting VXMQ", t));
  }

  public Uni<Void> stop() {
    return Uni.createFrom().voidItem()
      .onItem().transformToUni(v -> vertx.undeploy(mainVerticleId))
      .onItem().transformToUni(v -> vertx.close());
  }

  private Uni<Void> initVertx() {
    TcpDiscoverySpi tcpDiscoverySpi = new TcpDiscoverySpi();
    TcpDiscoveryMulticastIpFinder tcpDiscoveryMulticastIpFinder = new TcpDiscoveryMulticastIpFinder();
    Config.getIgniteDiscoveryTcpMulticastPort().ifPresent(tcpDiscoveryMulticastIpFinder::setMulticastPort);
    Config.getIgniteDiscoveryTcpMulticastGroup().ifPresent(tcpDiscoveryMulticastIpFinder::setMulticastGroup);
    Config.getIgniteDiscoveryTcpAddresses().map(s -> Arrays.stream(StringUtils.split(s, ",")).toList()).ifPresent(tcpDiscoveryMulticastIpFinder::setAddresses);

    Config.getIgniteDiscoveryTcpAddress().ifPresent(tcpDiscoverySpi::setLocalAddress);
    tcpDiscoverySpi.setLocalPort(Config.getIgniteDiscoveryTcpPort());
    tcpDiscoverySpi.setIpFinder(tcpDiscoveryMulticastIpFinder);

    IgniteConfiguration igniteConfiguration = new IgniteConfiguration();
    igniteConfiguration.setDiscoverySpi(tcpDiscoverySpi);
    igniteConfiguration.setGridLogger(new Slf4jLogger());
    igniteConfiguration.setMetricsLogFrequency(0);
    ClusterManager clusterManager = new IgniteClusterManager(igniteConfiguration);

    VertxOptions vertxOptions = buildVertxOptions();
    return Vertx.builder().with(vertxOptions).withClusterManager(clusterManager).buildClustered()
      .onItem().invoke(vtx -> this.vertx = vtx)
      .onItem().invoke(() -> {
        if (Config.getMetricsEnable()) {
          MeterRegistry registry = BackendRegistries.getDefaultNow();
          new UptimeMetrics().bindTo(registry);
          new FileDescriptorMetrics().bindTo(registry);
//          new LogbackMetrics().bindTo(registry);
          new MetricsFactory.PacketsPublishReceivedRateGaugeMetrics(10, vertx, MetricsFactory.getPacketsPublishReceivedCounter()).bindTo(registry);
          new MetricsFactory.PacketsPublishSentRateGaugeMetrics(10, vertx, MetricsFactory.getPacketsPublishSentCounter()).bindTo(registry);
        }
      })
      .replaceWithVoid();
  }

  private VertxOptions buildVertxOptions() {
    VertxOptions vertxOptions = new VertxOptions();
    if (Config.getMetricsEnable()) {
      VertxPrometheusOptions vertxPrometheusOptions = new VertxPrometheusOptions();
      vertxPrometheusOptions.setEnabled(true);
//    vertxPrometheusOptions.setPublishQuantiles(true);

      MicrometerMetricsOptions micrometerMetricsOptions = new MicrometerMetricsOptions();
      micrometerMetricsOptions.setEnabled(true);
      micrometerMetricsOptions.setJvmMetricsEnabled(true);
      micrometerMetricsOptions.setPrometheusOptions(vertxPrometheusOptions);
      vertxOptions.setMetricsOptions(micrometerMetricsOptions);
    }
    EventBusOptions eventBusOptions = new EventBusOptions();
    Config.getVertxEventbusHost().ifPresent(eventBusOptions::setHost);
    eventBusOptions.setPort(Config.getVertxEventbusPort());
    Config.getVertxEventbusPublicHost().ifPresent(eventBusOptions::setClusterPublicHost);
    eventBusOptions.setClusterPublicPort(Config.getVertxEventbusPublicPort());
    vertxOptions.setEventBusOptions(eventBusOptions);
    return vertxOptions;
  }

}
