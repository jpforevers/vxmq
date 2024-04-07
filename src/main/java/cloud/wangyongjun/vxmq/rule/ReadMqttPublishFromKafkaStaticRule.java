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
    String servers = Config.getRuleStaticReadMqttPublishFromKafkaKafkaServers(config());

    CompositeService compositeService = ServiceFactory.compositeService(vertx, config());
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
