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
import io.github.jpforevers.vxmq.assist.EBFactory;
import io.github.jpforevers.vxmq.metrics.MetricsFactory;
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
import org.apache.ignite.kubernetes.configuration.KubernetesConnectionConfiguration;
import org.apache.ignite.logger.slf4j.Slf4jLogger;
import org.apache.ignite.spi.discovery.tcp.TcpDiscoverySpi;
import org.apache.ignite.spi.discovery.tcp.ipfinder.TcpDiscoveryIpFinder;
import org.apache.ignite.spi.discovery.tcp.ipfinder.kubernetes.TcpDiscoveryKubernetesIpFinder;
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
      .onItem().invoke(() -> Runtime.getRuntime().addShutdownHook(new Thread(() -> vxmqLauncher.stop().subscribe().with(ConsumerUtil.nothingToDo(), Throwable::printStackTrace))))
      .subscribe().with(ConsumerUtil.nothingToDo(), ConsumerUtil.nothingToDo());
  }

  public Uni<Void> start() {
    Instant start = Instant.now();
    return Uni.createFrom().voidItem()
      .onItem().transformToUni(v -> initVertx())
      .onItem().invoke(v -> EBFactory.init(vertx))
      .onItem().transformToUni(v -> vertx.deployVerticle(MainVerticle.class.getName(), new DeploymentOptions()))
      .onItem().invoke(id -> this.mainVerticleId = id)
      .replaceWithVoid()
      .onItem().invoke(v -> LOGGER.info("VXMQ started in {} ms", Instant.now().toEpochMilli() - start.toEpochMilli()))
      .onFailure().invoke(t -> LOGGER.error("Error occurred when starting VXMQ", t));
  }

  public Uni<Void> stop() {
    LOGGER.info("Stopping VXMQ...");
    return Uni.createFrom().voidItem()
      .onItem().invoke(v -> MetricsFactory.clean())
      .onItem().transformToUni(v -> vertx.undeploy(mainVerticleId))
      .onItem().transformToUni(v -> vertx.close());
  }

  private Uni<Void> initVertx() {
    TcpDiscoverySpi tcpDiscoverySpi = new TcpDiscoverySpi();

    TcpDiscoveryIpFinder tcpDiscoveryIpFinder;
    Config.IgniteTcpDiscoveryIpFinderType igniteTcpDiscoveryIpFinderType = Config.getIgniteDiscoveryTcpIpFinderType();
    switch (igniteTcpDiscoveryIpFinderType) {
      case multicast -> {
        tcpDiscoveryIpFinder = new TcpDiscoveryMulticastIpFinder();
        Config.getIgniteDiscoveryTcpIpFinderMulticastPort().ifPresent(port -> ((TcpDiscoveryMulticastIpFinder) tcpDiscoveryIpFinder).setMulticastPort(port));
        Config.getIgniteDiscoveryTcpIpFinderMulticastGroup().ifPresent(group -> ((TcpDiscoveryMulticastIpFinder) tcpDiscoveryIpFinder).setMulticastGroup(group));
        Config.getIgniteDiscoveryTcpIpFinderMulticastAddresses().map(s -> Arrays.stream(StringUtils.split(s, ",")).toList()).ifPresent(addresses -> ((TcpDiscoveryMulticastIpFinder) tcpDiscoveryIpFinder).setAddresses(addresses));
      }
      case kubernetes -> {
        KubernetesConnectionConfiguration kubernetesConnectionConfiguration = new KubernetesConnectionConfiguration();
        kubernetesConnectionConfiguration.setDiscoveryPort(Config.getIgniteDiscoveryTcpPort());
        Config.getIgniteDiscoveryTcpIpFinderKubernetesNamespace().ifPresent(kubernetesConnectionConfiguration::setNamespace);
        Config.getIgniteDiscoveryTcpIpFinderKubernetesServicename().ifPresent(kubernetesConnectionConfiguration::setServiceName);
        tcpDiscoveryIpFinder = new TcpDiscoveryKubernetesIpFinder(kubernetesConnectionConfiguration);
      }
      default -> throw new IllegalArgumentException("Unsupported ignite discovery tcp ip finder type: " + igniteTcpDiscoveryIpFinderType);
    }

    Config.getIgniteDiscoveryTcpAddress().ifPresent(tcpDiscoverySpi::setLocalAddress);
    tcpDiscoverySpi.setLocalPort(Config.getIgniteDiscoveryTcpPort());
    tcpDiscoverySpi.setIpFinder(tcpDiscoveryIpFinder);

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
          MetricsFactory.init(vertx, BackendRegistries.getDefaultNow());
        }
      })
      .onItem().invoke(() -> {
        boolean isNativeTransportEnabled = vertx.isNativeTransportEnabled();
        LOGGER.info("Is native transport enabled: " + isNativeTransportEnabled);
        if (!isNativeTransportEnabled && vertx.unavailableNativeTransportCause() != null) {
          LOGGER.error("Unavailable native transport cause: ", vertx.unavailableNativeTransportCause());
        }
      })
      .replaceWithVoid();
  }

  private VertxOptions buildVertxOptions() {
    VertxOptions vertxOptions = new VertxOptions();
    if (Config.getMetricsEnable()) {
      VertxPrometheusOptions vertxPrometheusOptions = new VertxPrometheusOptions();
      vertxPrometheusOptions.setEnabled(true);
      vertxPrometheusOptions.setPublishQuantiles(true);

      MicrometerMetricsOptions micrometerMetricsOptions = new MicrometerMetricsOptions();
      micrometerMetricsOptions.setEnabled(true);
      micrometerMetricsOptions.setPrometheusOptions(vertxPrometheusOptions);
      vertxOptions.setMetricsOptions(micrometerMetricsOptions);
    }
    EventBusOptions eventBusOptions = new EventBusOptions();
    Config.getVertxEventbusHost().ifPresent(eventBusOptions::setHost);
    eventBusOptions.setPort(Config.getVertxEventbusPort());
    Config.getVertxEventbusPublicHost().ifPresent(eventBusOptions::setClusterPublicHost);
    eventBusOptions.setClusterPublicPort(Config.getVertxEventbusPublicPort());
    vertxOptions.setEventBusOptions(eventBusOptions);
    vertxOptions.setPreferNativeTransport(true);
    return vertxOptions;
  }

}
