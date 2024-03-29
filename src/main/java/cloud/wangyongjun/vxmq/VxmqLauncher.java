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

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.ConsoleAppender;
import ch.qos.logback.core.rolling.RollingFileAppender;
import ch.qos.logback.core.rolling.SizeAndTimeBasedRollingPolicy;
import ch.qos.logback.core.util.FileSize;
import cloud.wangyongjun.vxmq.assist.Config;
import io.smallrye.mutiny.Uni;
import io.vertx.config.ConfigRetrieverOptions;
import io.vertx.config.ConfigStoreOptions;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.VertxOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.core.spi.cluster.ClusterManager;
import io.vertx.mutiny.config.ConfigRetriever;
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

  public static void main(String[] args) {
    Instant start = Instant.now();
    Uni.createFrom().voidItem()
      .onItem().transformToUni(v -> retrieveConfig())
      .onItem().transformToUni(config -> Uni.createFrom().voidItem()
        .onItem().invoke(() -> configLog(config))
        .onItem().transformToUni(v -> startVertx(config))
        .onItem().call(vertx -> vertx.deployVerticle(MainVerticle.class.getName(), new DeploymentOptions().setConfig(config))))
      .subscribe().with(v -> LOGGER.info("VXMQ started in {} ms", Instant.now().toEpochMilli() - start.toEpochMilli()), t -> LOGGER.error("Error occurred when starting VXMQ", t));
  }

  /**
   * Retrieve config
   *
   * @return config
   */
  private static Uni<JsonObject> retrieveConfig() {
    ConfigRetrieverOptions configRetrieverOptions = new ConfigRetrieverOptions();
    // File application.properties config
    ConfigStoreOptions fileStore = new ConfigStoreOptions().setOptional(true).setType("file").setFormat("properties")
      .setConfig(new JsonObject().put("cache", false).put("path", "application.properties"));
    configRetrieverOptions.addStore(fileStore);
    //System property config
    ConfigStoreOptions sysStore = new ConfigStoreOptions().setOptional(true).setType("sys").setConfig(new JsonObject().put("cache", false));
    configRetrieverOptions.addStore(sysStore);
    // Environment variable config
    ConfigStoreOptions envStore = new ConfigStoreOptions().setOptional(true).setType("env");
    configRetrieverOptions.addStore(envStore);

    Vertx vertxTemp = Vertx.vertx();
    ConfigRetriever configRetriever = ConfigRetriever.create(vertxTemp, configRetrieverOptions);
    return configRetriever.getConfig()
      .onItem().invoke(config -> LOGGER.debug("Application config: {}", config.toString()))
      .eventually(vertxTemp::close);
  }

  private static void configLog(JsonObject config){
    String s = """
      <configuration scan="true" scanPeriod="60 seconds">

        <property name="PATTERN"
                  value="%date{yyyy-MM-dd'T'HH:mm:ss.SSS'Z', UTC} [%26.26thread] %-5level %-40.40logger{39} : %msg%n"/>
        <property name="LOG_DIR" value="${vxmq.logs.dir:-/vxmq/logs}"/>
        <property name="LOG_FILE" value="${LOG_DIR}/vxmq.log"/>

        <appender name="STDOUT" class="ch.qos.logback.core.ConsoleAppender">
          <encoder>
            <pattern>${PATTERN}</pattern>
          </encoder>
        </appender>

        <!-- From https://mkyong.com/logging/slf4j-logback-tutorial/ -->
        <appender name="FILE-ROLLING" class="ch.qos.logback.core.rolling.RollingFileAppender">
          <file>${LOG_FILE}</file>
          <rollingPolicy class="ch.qos.logback.core.rolling.SizeAndTimeBasedRollingPolicy">
            <fileNamePattern>${LOG_DIR}/archived/vxmq.%d{yyyy-MM-dd}.%i.log.gz</fileNamePattern>
            <!-- each archived file, size max 10MB -->
            <maxFileSize>100MB</maxFileSize>
            <!-- total size of all archive files, if total size > 20GB, it will delete old archived file -->
            <totalSizeCap>100GB</totalSizeCap>
            <!-- 30 days to keep -->
            <maxHistory>30</maxHistory>
          </rollingPolicy>
          <encoder>
            <pattern>${PATTERN}</pattern>
          </encoder>
        </appender>

        <logger name="org.apache.ignite" level="warn"/>

        <root level="info">
          <appender-ref ref="STDOUT"/>
          <appender-ref ref="FILE-ROLLING"/>
        </root>

      </configuration>
      """;
    // 等价于上面xml配置的代码配置：
    LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
    loggerContext.reset();

    String pattern = "%date{yyyy-MM-dd'T'HH:mm:ss.SSS'Z', UTC} [%26.26thread] %-5level %-40.40logger{39} : %msg%n";

    ConsoleAppender<ILoggingEvent> consoleAppender = new ConsoleAppender<>();
    consoleAppender.setContext(loggerContext);
    consoleAppender.setName("console");
    PatternLayoutEncoder consoleEncoder = new PatternLayoutEncoder();
    consoleEncoder.setContext(loggerContext);
    consoleEncoder.setPattern(pattern);
    consoleEncoder.start();
    consoleAppender.setEncoder(consoleEncoder);
    consoleAppender.start();

    ch.qos.logback.classic.Logger igniteLogger = loggerContext.getLogger("org.apache.ignite");
    igniteLogger.setLevel(Level.WARN);

    ch.qos.logback.classic.Logger kafkaLogger = loggerContext.getLogger("org.apache.kafka");
    kafkaLogger.setLevel(Level.WARN);

    ch.qos.logback.classic.Logger mqttLogger = loggerContext.getLogger("io.vertx.mqtt");
    mqttLogger.setLevel(Level.WARN);

    ch.qos.logback.classic.Logger vxmqLogger = loggerContext.getLogger("cloud.wangyongjun.vxmq");
    vxmqLogger.setLevel(Level.toLevel(Config.getLogsLevel(config)));

    ch.qos.logback.classic.Logger rootLogger = loggerContext.getLogger("ROOT");
    rootLogger.setLevel(Level.INFO);
    rootLogger.addAppender(consoleAppender);

  }

  /**
   * Start Vertx.
   *
   * @return Vertx
   */
  private static Uni<Vertx> startVertx(JsonObject config) {
    TcpDiscoverySpi tcpDiscoverySpi = new TcpDiscoverySpi();
    TcpDiscoveryMulticastIpFinder tcpDiscoveryMulticastIpFinder = new TcpDiscoveryMulticastIpFinder();
    tcpDiscoveryMulticastIpFinder.setAddresses(Arrays.stream(StringUtils.split(Config.getIgniteDiscoveryTcpAddresses(config), ",")).toList());
    tcpDiscoverySpi.setLocalPort(Config.getIgniteDiscoveryTcpPort(config));
    tcpDiscoverySpi.setIpFinder(tcpDiscoveryMulticastIpFinder);

    IgniteConfiguration igniteConfiguration = new IgniteConfiguration();
    igniteConfiguration.setDiscoverySpi(tcpDiscoverySpi);
    igniteConfiguration.setGridLogger(new Slf4jLogger());
    igniteConfiguration.setWorkDirectory(Config.getIgniteWorkDirectory(config));
    igniteConfiguration.setMetricsLogFrequency(0);
    ClusterManager clusterManager = new IgniteClusterManager(igniteConfiguration);

    VertxOptions vertxOptions = new VertxOptions();
    vertxOptions.setClusterManager(clusterManager);
    return Vertx.clusteredVertx(vertxOptions);
  }

}
