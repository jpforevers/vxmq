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

package cloud.wangyongjun.vxmq.rule;

import cloud.wangyongjun.vxmq.assist.Config;
import cloud.wangyongjun.vxmq.assist.ConsumerUtil;
import cloud.wangyongjun.vxmq.service.ServiceFactory;
import cloud.wangyongjun.vxmq.service.composite.CompositeService;
import cloud.wangyongjun.vxmq.service.msg.MsgToTopic;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.vertx.core.AbstractVerticle;
import io.vertx.core.json.JsonObject;
import io.vertx.kafka.client.serialization.JsonObjectDeserializer;
import io.vertx.mutiny.kafka.client.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class ReadMqttPublishFromKafkaStaticRule extends AbstractVerticle {

  private static final Logger LOGGER = LoggerFactory.getLogger(ReadMqttPublishFromKafkaStaticRule.class);

  private static final String KAFKA_TOPIC = "vxmq.mqtt.publish";

  private KafkaConsumer<String, JsonObject> kafkaConsumer;

  @Override
  public Uni<Void> asyncStart() {
    String servers = Config.getRuleStaticReadMqttPublishFromKafkaKafkaServers();

    CompositeService compositeService = ServiceFactory.compositeService(vertx);
    Map<String, String> kafkaConfig = new HashMap<>();
    kafkaConfig.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, servers);
    kafkaConfig.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
    kafkaConfig.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonObjectDeserializer.class.getName());
    kafkaConfig.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
    kafkaConfig.put(ConsumerConfig.GROUP_ID_CONFIG, "vxmq.rule.static.ReadMqttPublishFromKafka");
    kafkaConsumer = KafkaConsumer.create(vertx, kafkaConfig);
    kafkaConsumer.handler(record -> compositeService.forward(new MsgToTopic(record.value())).subscribe().with(ConsumerUtil.nothingToDo(), t -> LOGGER.error("Error occurred when forward msg from kafka", t)));

    return Uni.createFrom().voidItem()
      .onItem().transformToUni(v -> kafkaConsumer.subscribe(KAFKA_TOPIC));
  }

  @Override
  public Uni<Void> asyncStop() {
    return Uni.createFrom().voidItem();
  }

}
