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

  public static final String KEY_VXMQ_RULE_STATIC_READ_MQTT_PUBLISH_FROM_KAFKA_ENABLE = "vxmq.rule.static.ReadMqttPublishFromKafka.enable";
  public static final boolean DEFAULT_VXMQ_RULE_STATIC_READ_MQTT_PUBLISH_FROM_KAFKA_ENABLE = false;

  public static final String KEY_VXMQ_RULE_STATIC_READ_MQTT_PUBLISH_FROM_KAFKA_KAFKA_SERVERS = "vxmq.rule.static.ReadMqttPublishFromKafka.kafka.servers";
  public static final String DEFAULT_VXMQ_RULE_STATIC_READ_MQTT_PUBLISH_FROM_KAFKA_KAFKA_SERVERS = "localhost:9094";

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

  public static boolean getRuleStaticReadMqttPublishFromKafkaEnable(JsonObject config){
    return config.getBoolean(KEY_VXMQ_RULE_STATIC_READ_MQTT_PUBLISH_FROM_KAFKA_ENABLE, DEFAULT_VXMQ_RULE_STATIC_READ_MQTT_PUBLISH_FROM_KAFKA_ENABLE);
  }

  public static String getRuleStaticReadMqttPublishFromKafkaKafkaServers(JsonObject config){
    return config.getString(KEY_VXMQ_RULE_STATIC_READ_MQTT_PUBLISH_FROM_KAFKA_KAFKA_SERVERS, DEFAULT_VXMQ_RULE_STATIC_READ_MQTT_PUBLISH_FROM_KAFKA_KAFKA_SERVERS);
  }

}
