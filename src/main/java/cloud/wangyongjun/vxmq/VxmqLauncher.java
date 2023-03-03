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
import org.apache.ignite.configuration.IgniteConfiguration;
import org.apache.ignite.logger.slf4j.Slf4jLogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;

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
    String logDir = Config.getLogsDir(config);
    String logFile = Config.getLogFile(config);

    ConsoleAppender<ILoggingEvent> consoleAppender = new ConsoleAppender<>();
    consoleAppender.setContext(loggerContext);
    consoleAppender.setName("console");
    PatternLayoutEncoder consoleEncoder = new PatternLayoutEncoder();
    consoleEncoder.setContext(loggerContext);
    consoleEncoder.setPattern(pattern);
    consoleEncoder.start();
    consoleAppender.setEncoder(consoleEncoder);
    consoleAppender.start();

    RollingFileAppender<ILoggingEvent> rollingFileAppender = new RollingFileAppender<>();
    rollingFileAppender.setContext(loggerContext);
    rollingFileAppender.setName("rollingFile");
    rollingFileAppender.setFile(logFile);

    SizeAndTimeBasedRollingPolicy<ILoggingEvent> sizeAndTimeBasedRollingPolicy = new SizeAndTimeBasedRollingPolicy<>();
    sizeAndTimeBasedRollingPolicy.setContext(loggerContext);
    sizeAndTimeBasedRollingPolicy.setParent(rollingFileAppender);
    sizeAndTimeBasedRollingPolicy.setFileNamePattern(logDir + "/archived/vxmq.%d{yyyy-MM-dd}.%i.log.gz");
    sizeAndTimeBasedRollingPolicy.setMaxFileSize(FileSize.valueOf("10MB"));
    sizeAndTimeBasedRollingPolicy.setTotalSizeCap(FileSize.valueOf("100GB"));
    sizeAndTimeBasedRollingPolicy.setMaxHistory(30);
    sizeAndTimeBasedRollingPolicy.start();

    PatternLayoutEncoder fileEncoder = new PatternLayoutEncoder();
    fileEncoder.setContext(loggerContext);
    fileEncoder.setPattern(pattern);
    fileEncoder.start();

    rollingFileAppender.setEncoder(fileEncoder);
    rollingFileAppender.setRollingPolicy(sizeAndTimeBasedRollingPolicy);
    rollingFileAppender.setTriggeringPolicy(sizeAndTimeBasedRollingPolicy);
    rollingFileAppender.start();

    ch.qos.logback.classic.Logger customLogger = loggerContext.getLogger("org.apache.ignite");
    customLogger.setLevel(Level.WARN);

    ch.qos.logback.classic.Logger rootLogger = loggerContext.getLogger("ROOT");
    rootLogger.setLevel(Level.INFO);
    rootLogger.addAppender(consoleAppender);
    rootLogger.addAppender(rollingFileAppender);

  }

  /**
   * Start Vertx
   *
   * @return Vertx
   */
  private static Uni<Vertx> startVertx(JsonObject config) {
//    TcpDiscoverySpi spi = new TcpDiscoverySpi();
//    TcpDiscoveryMulticastIpFinder ipFinder = new TcpDiscoveryMulticastIpFinder();
//    ipFinder.setMulticastGroup("228.10.10.157");
//    spi.setIpFinder(ipFinder);

    IgniteConfiguration igniteCfg = new IgniteConfiguration();
    igniteCfg.setGridLogger(new Slf4jLogger());
    igniteCfg.setWorkDirectory(Config.getIgniteWorkDirectory(config));
    ClusterManager clusterManager = new IgniteClusterManager(igniteCfg);

    VertxOptions vertxOptions = new VertxOptions();
    vertxOptions.setClusterManager(clusterManager);
    return Vertx.clusteredVertx(vertxOptions);
  }

}
