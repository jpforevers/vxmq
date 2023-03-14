package cloud.wangyongjun.vxmq.rule;

import cloud.wangyongjun.vxmq.assist.Config;
import cloud.wangyongjun.vxmq.assist.ConsumerUtil;
import cloud.wangyongjun.vxmq.assist.ServiceFactory;
import cloud.wangyongjun.vxmq.event.Event;
import cloud.wangyongjun.vxmq.event.EventService;
import cloud.wangyongjun.vxmq.event.EventType;
import cloud.wangyongjun.vxmq.event.MqttEvent;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.vertx.core.AbstractVerticle;
import io.vertx.core.json.JsonObject;
import io.vertx.kafka.admin.NewTopic;
import io.vertx.kafka.client.serialization.JsonObjectSerializer;
import io.vertx.mutiny.kafka.admin.KafkaAdminClient;
import io.vertx.mutiny.kafka.client.producer.KafkaProducer;
import io.vertx.mutiny.kafka.client.producer.KafkaProducerRecord;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class WriteMqttEventToKafkaStaticRule extends AbstractVerticle {

  private static final Logger LOGGER = LoggerFactory.getLogger(WriteMqttEventToKafkaStaticRule.class);

  @Override
  public Uni<Void> asyncStart() {
    String servers = Config.getRuleStaticWriteMqttEventToKafkaKafkaServers(config());
    String kafkaTopic = Config.getRuleStaticWriteMqttEventToKafkaKafkaTopic(config());

    Map<String, String> adminConfig = new HashMap<>();
    adminConfig.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, servers);
    KafkaAdminClient kafkaAdminClient = KafkaAdminClient.create(vertx, adminConfig);

    EventService eventService = ServiceFactory.eventService(vertx);
    Map<String, String> kafkaConfig = new HashMap<>();
    kafkaConfig.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, servers);
    kafkaConfig.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
    kafkaConfig.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonObjectSerializer.class.getName());
    kafkaConfig.put(ProducerConfig.ACKS_CONFIG, "1");
    KafkaProducer<String, JsonObject> kafkaProducer = KafkaProducer.create(vertx, kafkaConfig);

    List<Uni<Void>> consumeEventUnis = new ArrayList<>();
    for (EventType value : EventType.values()) {
      Uni<Void> consumeEventUni = eventService.consumeEvent(value, data -> {
        Event event = value.fromJson(data);
        if (event instanceof MqttEvent) {
          KafkaProducerRecord<String, JsonObject> record = KafkaProducerRecord.create(kafkaTopic, ((MqttEvent) event).getClientId(), data);
          kafkaProducer.write(record).subscribe().with(ConsumerUtil.nothingToDo(), t -> LOGGER.error("Error occurred when write record to kafka", t));
        }
      }, true).replaceWithVoid();
      consumeEventUnis.add(consumeEventUni);
    }

    return Uni.createFrom().voidItem()
      .onItem().transformToUni(v -> kafkaAdminClient.listTopics())
      .onItem().transformToUni(topics -> topics.contains(kafkaTopic) ? Uni.createFrom().voidItem() : kafkaAdminClient.createTopics(Collections.singletonList(new NewTopic(kafkaTopic, 1, (short) 1))))
      .onItem().transformToUni(v -> Uni.combine().all().unis(consumeEventUnis).discardItems());
  }

  @Override
  public Uni<Void> asyncStop() {
    return Uni.createFrom().voidItem();
  }

}
