package cloud.wangyongjun.vxmq.assist;

import io.vertx.core.json.JsonObject;

public class Config {

  public static final String KEY_NOTHING = "nothing";
  public static final String DEFAULT_NOTHING = "default_nothing";
  public static final String KEY_VXMQ_HTTP_SERVER_PORT = "vxmq.http.server.port";
  public static final int DEFAULT_VXMQ_HTTP_SERVER_PORT = 8060;
  public static final String KEY_VXMQ_MQTT_SERVER_PORT = "vxmq.mqtt.server.port";
  public static final int DEFAULT_VXMQ_MQTT_SERVER_PORT = 1883;
  public static final String KEY_VXMQ_SHELL_SERVER_PORT = "vxmq.shell.server.port";
  public static final int DEFAULT_VXMQ_SHELL_SERVER_PORT = 5000;

  public static final String KEY_VXMQ_SESSION_QUEUED_MESSAGE_MAX = "vxmq.session.queued-message.max";
  public static final int DEFAULT_VXMQ_SESSION_QUEUED_MESSAGE_MAX = 1000;

  public static final String KEY_VXMQ_SUB_IGNITE_BACKUPS = "vxmq.sub.ignite.backups";
  public static final int DEFAULT_VXMQ_SUB_IGNITE_BACKUPS = 1;

  public static final String KEY_VXMQ_LOGS_DIR = "vxmq.logs.dir";
  public static final String DEFAULT_VXMQ_LOGS_DIR = "/vxmq/logs";

  public static final String KEY_VXMQ_IGNITE_WORK_DIR = "vxmq.ignite.work-dir";
  public static final String DEFAULT_VXMQ_IGNITE_WORK_DIR = "/vxmq/ignite";

  public static final String KEY_VXMQ_RULE_STATIC_WRITE_MQTT_EVENT_TO_KAFKA_ENABLE = "vxmq.rule.static.WriteMqttEventToKafka.enable";
  public static final boolean DEFAULT_VXMQ_RULE_STATIC_WRITE_MQTT_EVENT_TO_KAFKA_ENABLE = false;

  public static final String KEY_VXMQ_RULE_STATIC_WRITE_MQTT_EVENT_TO_KAFKA_KAFKA_SERVERS = "vxmq.rule.static.WriteMqttEventToKafka.kafka.servers";
  public static final String DEFAULT_VXMQ_RULE_STATIC_WRITE_MQTT_EVENT_TO_KAFKA_KAFKA_SERVERS = "localhost:9094";

  public static final String KEY_VXMQ_RULE_STATIC_WRITE_MQTT_EVENT_TO_KAFKA_KAFKA_TOPIC = "vxmq.rule.static.WriteMqttEventToKafka.kafka.topic";
  public static final String DEFAULT_VXMQ_RULE_STATIC_WRITE_MQTT_EVENT_TO_KAFKA_KAFKA_TOPIC = "vxmq.mqtt.event";

  public static String getNothing(JsonObject config){
    return config.getString(KEY_NOTHING, DEFAULT_NOTHING);
  }

  public static int getHttpServerPort(JsonObject config) {
    return config.getInteger(KEY_VXMQ_HTTP_SERVER_PORT, DEFAULT_VXMQ_HTTP_SERVER_PORT);
  }

  public static int getMqttServerPort(JsonObject config) {
    return config.getInteger(KEY_VXMQ_MQTT_SERVER_PORT, DEFAULT_VXMQ_MQTT_SERVER_PORT);
  }

  public static int getShellServerPort(JsonObject config) {
    return config.getInteger(KEY_VXMQ_SHELL_SERVER_PORT, DEFAULT_VXMQ_SHELL_SERVER_PORT);
  }

  public static int getSessionQueuedMessageMax(JsonObject config) {
    return config.getInteger(KEY_VXMQ_SESSION_QUEUED_MESSAGE_MAX, DEFAULT_VXMQ_SESSION_QUEUED_MESSAGE_MAX);
  }

  public static int getSubIgniteBackups(JsonObject config) {
    return config.getInteger(KEY_VXMQ_SUB_IGNITE_BACKUPS, DEFAULT_VXMQ_SUB_IGNITE_BACKUPS);
  }

  public static String getIgniteWorkDirectory(JsonObject config){
    return config.getString(KEY_VXMQ_IGNITE_WORK_DIR, DEFAULT_VXMQ_IGNITE_WORK_DIR);
  }

  public static String getLogsDir(JsonObject config){
    return config.getString(KEY_VXMQ_LOGS_DIR, DEFAULT_VXMQ_LOGS_DIR);
  }

  public static String getLogFile(JsonObject config){
    return getLogsDir(config) + "/vxmq.log";
  }

  public static boolean getRuleStaticWriteMqttEventToKafkaEnable(JsonObject config){
    return config.getBoolean(KEY_VXMQ_RULE_STATIC_WRITE_MQTT_EVENT_TO_KAFKA_ENABLE, DEFAULT_VXMQ_RULE_STATIC_WRITE_MQTT_EVENT_TO_KAFKA_ENABLE);
  }

  public static String getRuleStaticWriteMqttEventToKafkaKafkaServers(JsonObject config){
    return config.getString(KEY_VXMQ_RULE_STATIC_WRITE_MQTT_EVENT_TO_KAFKA_KAFKA_SERVERS, DEFAULT_VXMQ_RULE_STATIC_WRITE_MQTT_EVENT_TO_KAFKA_KAFKA_SERVERS);
  }

  public static String getRuleStaticWriteMqttEventToKafkaKafkaTopic(JsonObject config){
    return config.getString(KEY_VXMQ_RULE_STATIC_WRITE_MQTT_EVENT_TO_KAFKA_KAFKA_TOPIC, DEFAULT_VXMQ_RULE_STATIC_WRITE_MQTT_EVENT_TO_KAFKA_KAFKA_TOPIC);
  }

}
