/*
 * Copyright 2018-present 王用军
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package cloud.wangyongjun.vxmq;

import cloud.wangyongjun.vxmq.assist.Config;
import cloud.wangyongjun.vxmq.assist.ConsumerUtil;
import io.smallrye.mutiny.Uni;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.VertxOptions;
import io.vertx.core.spi.cluster.ClusterManager;
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
      .onItem().transformToUni(v -> deployMainVerticle())
      .onItem().invoke(v -> LOGGER.info("VXMQ started in {} ms", Instant.now().toEpochMilli() - start.toEpochMilli()))
      .onFailure().invoke(t -> LOGGER.error("Error occurred when starting VXMQ", t));
  }

  private Uni<Void> initVertx() {
    TcpDiscoverySpi tcpDiscoverySpi = new TcpDiscoverySpi();
    TcpDiscoveryMulticastIpFinder tcpDiscoveryMulticastIpFinder = new TcpDiscoveryMulticastIpFinder();
    tcpDiscoveryMulticastIpFinder.setAddresses(Arrays.stream(StringUtils.split(Config.getIgniteDiscoveryTcpAddresses(), ",")).toList());
    tcpDiscoverySpi.setLocalPort(Config.getIgniteDiscoveryTcpPort());
    tcpDiscoverySpi.setIpFinder(tcpDiscoveryMulticastIpFinder);

    IgniteConfiguration igniteConfiguration = new IgniteConfiguration();
    igniteConfiguration.setDiscoverySpi(tcpDiscoverySpi);
    igniteConfiguration.setGridLogger(new Slf4jLogger());
    igniteConfiguration.setWorkDirectory(Config.getIgniteWorkDirectory());
    igniteConfiguration.setMetricsLogFrequency(0);
    ClusterManager clusterManager = new IgniteClusterManager(igniteConfiguration);

    VertxOptions vertxOptions = new VertxOptions();
    return Vertx.builder().with(vertxOptions).withClusterManager(clusterManager).buildClustered()
      .onItem().invoke(vtx -> this.vertx = vtx)
      .replaceWithVoid();
  }

  private Uni<Void> deployMainVerticle() {
    return Uni.createFrom().voidItem()
      .onItem().transformToUni(v -> vertx.deployVerticle(MainVerticle.class.getName(), new DeploymentOptions()))
      .replaceWithVoid();
  }

}
