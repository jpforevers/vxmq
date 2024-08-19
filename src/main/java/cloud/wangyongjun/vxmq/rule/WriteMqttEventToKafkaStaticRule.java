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
import cloud.wangyongjun.vxmq.event.Event;
import cloud.wangyongjun.vxmq.event.EventService;
import cloud.wangyongjun.vxmq.event.EventType;
import cloud.wangyongjun.vxmq.event.mqtt.MqttEvent;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.vertx.core.AbstractVerticle;
import io.vertx.core.json.JsonObject;
import io.vertx.kafka.client.serialization.JsonObjectSerializer;
import io.vertx.mutiny.kafka.client.producer.KafkaProducer;
import io.vertx.mutiny.kafka.client.producer.KafkaProducerRecord;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WriteMqttEventToKafkaStaticRule extends AbstractVerticle {

  private static final Logger LOGGER = LoggerFactory.getLogger(WriteMqttEventToKafkaStaticRule.class);

  private static final String KAFKA_TOPIC_PREFIX = "vxmq";


  private EventService eventService;
  private KafkaProducer<String, JsonObject> kafkaProducer;

  @Override
  public Uni<Void> asyncStart() {
    String servers = Config.getRuleStaticWriteMqttEventToKafkaKafkaServers(config());

    eventService = ServiceFactory.eventService(vertx);
    Map<String, String> kafkaConfig = new HashMap<>();
    kafkaConfig.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, servers);
    kafkaConfig.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
    kafkaConfig.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonObjectSerializer.class.getName());
    kafkaConfig.put(ProducerConfig.ACKS_CONFIG, "1");
    kafkaConfig.put(ProducerConfig.MAX_REQUEST_SIZE_CONFIG, "104857600"); // Default 1M, change to 100M
    kafkaProducer = KafkaProducer.createShared(vertx, "vxmq.rule.static.WriteMqttEventToKafka", kafkaConfig);

    List<Uni<Void>> consumeEventUnis = new ArrayList<>();
    for (EventType value : EventType.values()) {
      Uni<Void> consumeEventUni = eventService.consumeEvent(value, data -> {
        if (LOGGER.isDebugEnabled()){
          LOGGER.debug("Event consumed: {}", data);
        }
        Event event = value.fromJson(data);
        if (event instanceof MqttEvent) {
          KafkaProducerRecord<String, JsonObject> record = KafkaProducerRecord.create(genKafkaTopicFromEventType(value), ((MqttEvent) event).getClientId(), data);
          kafkaProducer.write(record).subscribe().with(ConsumerUtil.nothingToDo(), t -> LOGGER.error("Error occurred when write record to kafka", t));
        }
      }, true).replaceWithVoid();
      consumeEventUnis.add(consumeEventUni);
    }

    return Uni.createFrom().voidItem()
      .onItem().transformToUni(v -> Uni.combine().all().unis(consumeEventUnis).collectFailures().discardItems());
  }

  @Override
  public Uni<Void> asyncStop() {
    return Uni.createFrom().voidItem();
  }

  private String genKafkaTopicFromEventType(EventType eventType) {
    return KAFKA_TOPIC_PREFIX + "." + eventType.name().toLowerCase().replace('_', '.');
  }

}
