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

package cloud.wangyongjun.vxmq.assist;

import cloud.wangyongjun.vxmq.service.authentication.MqttAuthType;
import io.smallrye.config.SmallRyeConfig;
import io.vertx.core.impl.cpu.CpuCoreSensor;
import org.apache.commons.lang3.StringUtils;
import org.apache.ignite.spi.discovery.tcp.TcpDiscoverySpi;
import org.eclipse.microprofile.config.ConfigProvider;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

public class Config {

  public static final int AVAILABLE_CPU_CORE_SENSORS = CpuCoreSensor.availableProcessors();

  private static final SmallRyeConfig smallRyeConfig = ConfigProvider.getConfig().unwrap(SmallRyeConfig.class);

  public static final String KEY_VXMQ_HTTP_SERVER_PORT = "vxmq.http.server.port";
  public static final int DEFAULT_VXMQ_HTTP_SERVER_PORT = 8060;
  public static final String KEY_VXMQ_MQTT_SERVER_PORT = "vxmq.mqtt.server.port";
  public static final int DEFAULT_VXMQ_MQTT_SERVER_PORT = 1883;
  public static final String KEY_VXMQ_MQTT_SERVER_PROXY_PROTOCOL_ENABLE = "vxmq.mqtt.server.proxy-protocol.enable";
  public static final boolean DEFAULT_VXMQ_MQTT_SERVER_PROXY_PROTOCOL_ENABLE = false;
  public static final String KEY_VXMQ_MQTT_AUTH_WHITELIST = "vxmq.mqtt.auth.whitelist";
  public static final String DEFAULT_VXMQ_MQTT_AUTH_WHITELIST = "";
  public static final String KEY_VXMQ_MQTT_AUTH_TYPE = "vxmq.mqtt.auth.type";
  public static final String DEFAULT_VXMQ_MQTT_AUTH_TYPE = MqttAuthType.NONE.name();
  public static final String KEY_VXMQ_MQTT_AUTH_WEBHOOK_URL = "vxmq.mqtt.auth.webhook.url";
  public static final String DEFAULT_VXMQ_MQTT_AUTH_WEBHOOK_URL = "http://localhost:8080";
  public static final String KEY_VXMQ_MQTT_TOPIC_ALIAS_MAXIMUM = "vxmq.mqtt.topic.alias.maximum";
  public static final int DEFAULT_VXMQ_MQTT_TOPIC_ALIAS_MAXIMUM = 65535;
  public static final String KEY_VXMQ_MQTT_MESSAGE_SIZE_MAX = "vxmq.mqtt.message.size.max";
  public static final int DEFAULT_VXMQ_MQTT_MESSAGE_SIZE_MAX = 10 * 1024 * 1024;  // 10M
  public static final String KEY_VXMQ_MQTT_CLIENT_ID_LENGTH_MAX = "vxmq.mqtt.client-id.length.max";
  public static final int DEFAULT_VXMQ_MQTT_CLIENT_ID_LENGTH_MAX = 128;

  public static final String KEY_VXMQ_SHELL_SERVER_PORT = "vxmq.shell.server.port";
  public static final int DEFAULT_VXMQ_SHELL_SERVER_PORT = 5000;

  public static final String KEY_VXMQ_SESSION_QUEUED_MESSAGE_MAX = "vxmq.session.queued-message.max";
  public static final int DEFAULT_VXMQ_SESSION_QUEUED_MESSAGE_MAX = 1000;

  public static final String KEY_VXMQ_IGNITE_BACKUPS = "vxmq.ignite.backups";
  public static final int DEFAULT_VXMQ_IGNITE_BACKUPS = 1;

  public static final String KEY_VXMQ_IGNITE_DISCOVERY_TCP_PORT = "vxmq.ignite.discovery.tcp.port";
  public static final int DEFAULT_VXMQ_IGNITE_DISCOVERY_TCP_PORT = TcpDiscoverySpi.DFLT_PORT;

  public static final String KEY_VXMQ_IGNITE_DISCOVERY_TCP_ADDRESSES = "vxmq.ignite.discovery.tcp.addresses";
  public static final String DEFAULT_VXMQ_IGNITE_DISCOVERY_TCP_ADDRESSES = "localhost:47500";

  public static final String KEY_VXMQ_RULE_STATIC_WRITE_MQTT_EVENT_TO_MQTT_ENABLE = "vxmq.rule.static.WriteMqttEventToMqtt.enable";
  public static final boolean DEFAULT_VXMQ_RULE_STATIC_WRITE_MQTT_EVENT_TO_MQTT_ENABLE = false;

  public static final String KEY_VXMQ_RULE_STATIC_WRITE_MQTT_EVENT_TO_KAFKA_ENABLE = "vxmq.rule.static.WriteMqttEventToKafka.enable";
  public static final boolean DEFAULT_VXMQ_RULE_STATIC_WRITE_MQTT_EVENT_TO_KAFKA_ENABLE = false;

  public static final String KEY_VXMQ_RULE_STATIC_WRITE_MQTT_EVENT_TO_KAFKA_KAFKA_SERVERS = "vxmq.rule.static.WriteMqttEventToKafka.kafka.servers";
  public static final String DEFAULT_VXMQ_RULE_STATIC_WRITE_MQTT_EVENT_TO_KAFKA_KAFKA_SERVERS = "localhost:9094";

  public static final String KEY_VXMQ_RULE_STATIC_READ_MQTT_PUBLISH_FROM_KAFKA_ENABLE = "vxmq.rule.static.ReadMqttPublishFromKafka.enable";
  public static final boolean DEFAULT_VXMQ_RULE_STATIC_READ_MQTT_PUBLISH_FROM_KAFKA_ENABLE = false;

  public static final String KEY_VXMQ_RULE_STATIC_READ_MQTT_PUBLISH_FROM_KAFKA_KAFKA_SERVERS = "vxmq.rule.static.ReadMqttPublishFromKafka.kafka.servers";
  public static final String DEFAULT_VXMQ_RULE_STATIC_READ_MQTT_PUBLISH_FROM_KAFKA_KAFKA_SERVERS = "localhost:9094";

  public static final String KEY_VXMQ_METRICS_ENABLE = "vxmq.metrics.enable";
  public static final boolean DEFAULT_VXMQ_METRICS_ENABLE = false;

  public static int getHttpServerPort() {
    return smallRyeConfig.getOptionalValue(KEY_VXMQ_HTTP_SERVER_PORT, Integer.class).orElse(DEFAULT_VXMQ_HTTP_SERVER_PORT);
  }

  public static int getMqttServerPort() {
    return smallRyeConfig.getOptionalValue(KEY_VXMQ_MQTT_SERVER_PORT, Integer.class).orElse(DEFAULT_VXMQ_MQTT_SERVER_PORT);
  }

  public static boolean getMqttProxyProtocolEnable() {
    return smallRyeConfig.getOptionalValue(KEY_VXMQ_MQTT_SERVER_PROXY_PROTOCOL_ENABLE, Boolean.class).orElse(DEFAULT_VXMQ_MQTT_SERVER_PROXY_PROTOCOL_ENABLE);
  }

  public static Set<String> getMqttAuthWhitelist() {
    String mqttAuthWhiteListString = smallRyeConfig.getOptionalValue(KEY_VXMQ_MQTT_AUTH_WHITELIST, String.class).orElse(DEFAULT_VXMQ_MQTT_AUTH_WHITELIST);
    return StringUtils.isNotBlank(mqttAuthWhiteListString) ? Arrays.stream(StringUtils.split(mqttAuthWhiteListString, ',')).collect(Collectors.toSet()) : Set.of();
  }

  public static MqttAuthType getMqttAuthType() {
    return MqttAuthType.valueOf(smallRyeConfig.getOptionalValue(KEY_VXMQ_MQTT_AUTH_TYPE, String.class).orElse(DEFAULT_VXMQ_MQTT_AUTH_TYPE));
  }

  public static String getMqttAuthWebhookUrl() {
    return smallRyeConfig.getOptionalValue(KEY_VXMQ_MQTT_AUTH_WEBHOOK_URL, String.class).orElse(DEFAULT_VXMQ_MQTT_AUTH_WEBHOOK_URL);
  }

  public static int getMqttTopicAliasMaximum() {
    return smallRyeConfig.getOptionalValue(KEY_VXMQ_MQTT_TOPIC_ALIAS_MAXIMUM, Integer.class).orElse(DEFAULT_VXMQ_MQTT_TOPIC_ALIAS_MAXIMUM);
  }

  public static int getMqttMessageSizeMax() {
    return smallRyeConfig.getOptionalValue(KEY_VXMQ_MQTT_MESSAGE_SIZE_MAX, Integer.class).orElse(DEFAULT_VXMQ_MQTT_MESSAGE_SIZE_MAX);
  }

  public static int getMqttClientIdLengthMax() {
    return smallRyeConfig.getOptionalValue(KEY_VXMQ_MQTT_CLIENT_ID_LENGTH_MAX, Integer.class).orElse(DEFAULT_VXMQ_MQTT_CLIENT_ID_LENGTH_MAX);
  }

  public static int getShellServerPort() {
    return smallRyeConfig.getOptionalValue(KEY_VXMQ_SHELL_SERVER_PORT, Integer.class).orElse(DEFAULT_VXMQ_SHELL_SERVER_PORT);
  }

  public static int getSessionQueuedMessageMax() {
    return smallRyeConfig.getOptionalValue(KEY_VXMQ_SESSION_QUEUED_MESSAGE_MAX, Integer.class).orElse(DEFAULT_VXMQ_SESSION_QUEUED_MESSAGE_MAX);
  }

  public static int getIgniteBackups() {
    return smallRyeConfig.getOptionalValue(KEY_VXMQ_IGNITE_BACKUPS, Integer.class).orElse(DEFAULT_VXMQ_IGNITE_BACKUPS);
  }

  public static int getIgniteDiscoveryTcpPort() {
    return smallRyeConfig.getOptionalValue(KEY_VXMQ_IGNITE_DISCOVERY_TCP_PORT, Integer.class).orElse(DEFAULT_VXMQ_IGNITE_DISCOVERY_TCP_PORT);
  }

  public static String getIgniteDiscoveryTcpAddresses() {
    return smallRyeConfig.getOptionalValue(KEY_VXMQ_IGNITE_DISCOVERY_TCP_ADDRESSES, String.class).orElse(DEFAULT_VXMQ_IGNITE_DISCOVERY_TCP_ADDRESSES);
  }

  public static boolean getRuleStaticWriteMqttEventToMqttEnable() {
    return smallRyeConfig.getOptionalValue(KEY_VXMQ_RULE_STATIC_WRITE_MQTT_EVENT_TO_MQTT_ENABLE, Boolean.class).orElse(DEFAULT_VXMQ_RULE_STATIC_WRITE_MQTT_EVENT_TO_MQTT_ENABLE);
  }

  public static boolean getRuleStaticWriteMqttEventToKafkaEnable() {
    return smallRyeConfig.getOptionalValue(KEY_VXMQ_RULE_STATIC_WRITE_MQTT_EVENT_TO_KAFKA_ENABLE, Boolean.class).orElse(DEFAULT_VXMQ_RULE_STATIC_WRITE_MQTT_EVENT_TO_KAFKA_ENABLE);
  }

  public static String getRuleStaticWriteMqttEventToKafkaKafkaServers() {
    return smallRyeConfig.getOptionalValue(KEY_VXMQ_RULE_STATIC_WRITE_MQTT_EVENT_TO_KAFKA_KAFKA_SERVERS, String.class).orElse(DEFAULT_VXMQ_RULE_STATIC_WRITE_MQTT_EVENT_TO_KAFKA_KAFKA_SERVERS);
  }

  public static boolean getRuleStaticReadMqttPublishFromKafkaEnable() {
    return smallRyeConfig.getOptionalValue(KEY_VXMQ_RULE_STATIC_READ_MQTT_PUBLISH_FROM_KAFKA_ENABLE, Boolean.class).orElse(DEFAULT_VXMQ_RULE_STATIC_READ_MQTT_PUBLISH_FROM_KAFKA_ENABLE);
  }

  public static String getRuleStaticReadMqttPublishFromKafkaKafkaServers() {
    return smallRyeConfig.getOptionalValue(KEY_VXMQ_RULE_STATIC_READ_MQTT_PUBLISH_FROM_KAFKA_KAFKA_SERVERS, String.class).orElse(DEFAULT_VXMQ_RULE_STATIC_READ_MQTT_PUBLISH_FROM_KAFKA_KAFKA_SERVERS);
  }

  public static boolean getMetricsEnable() {
    return smallRyeConfig.getOptionalValue(KEY_VXMQ_METRICS_ENABLE, Boolean.class).orElse(DEFAULT_VXMQ_METRICS_ENABLE);
  }

}
