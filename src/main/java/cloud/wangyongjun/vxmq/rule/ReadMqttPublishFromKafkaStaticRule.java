package cloud.wangyongjun.vxmq.rule;

import cloud.wangyongjun.vxmq.assist.Config;
import cloud.wangyongjun.vxmq.assist.ConsumerUtil;
import cloud.wangyongjun.vxmq.assist.ServiceFactory;
import cloud.wangyongjun.vxmq.mqtt.composite.CompositeService;
import cloud.wangyongjun.vxmq.mqtt.msg.MsgToTopic;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.vertx.core.AbstractVerticle;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;
import io.vertx.kafka.admin.NewTopic;
import io.vertx.kafka.client.serialization.BufferDeserializer;
import io.vertx.kafka.client.serialization.JsonObjectDeserializer;
import io.vertx.mutiny.kafka.admin.KafkaAdminClient;
import io.vertx.mutiny.kafka.client.consumer.KafkaConsumer;
import io.vertx.mutiny.kafka.client.producer.KafkaHeader;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class ReadMqttPublishFromKafkaStaticRule extends AbstractVerticle {

  private static final Logger LOGGER = LoggerFactory.getLogger(ReadMqttPublishFromKafkaStaticRule.class);

  @Override
  public Uni<Void> asyncStart() {
    String servers = Config.getRuleStaticReadMqttPublishFromKafkaKafkaServers(config());
    String kafkaTopic = Config.getRuleStaticReadMqttPublishFromKafkaKafkaTopic(config());

    Map<String, String> adminConfig = new HashMap<>();
    adminConfig.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, servers);
    KafkaAdminClient kafkaAdminClient = KafkaAdminClient.create(vertx, adminConfig);

    CompositeService compositeService = ServiceFactory.compositeService(vertx, config());
    Map<String, String> kafkaConfig = new HashMap<>();
    kafkaConfig.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, servers);
    kafkaConfig.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
    kafkaConfig.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonObjectDeserializer.class.getName());
    kafkaConfig.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
    kafkaConfig.put(ConsumerConfig.GROUP_ID_CONFIG, ReadMqttPublishFromKafkaStaticRule.class.getSimpleName());
    KafkaConsumer<String, JsonObject> kafkaConsumer = KafkaConsumer.create(vertx, kafkaConfig);
    kafkaConsumer.handler(record -> {
      compositeService.forward(new MsgToTopic(record.value())).subscribe().with(ConsumerUtil.nothingToDo(), t -> LOGGER.error("Error occurred when forward msg from kafka", t));
    });

    return Uni.createFrom().voidItem()
      .onItem().transformToUni(v -> kafkaAdminClient.listTopics())
      .onItem().transformToUni(topics -> topics.contains(kafkaTopic) ? Uni.createFrom().voidItem() : kafkaAdminClient.createTopics(Collections.singletonList(new NewTopic(kafkaTopic, 1, (short) 1))))
      .onItem().transformToUni(v -> kafkaConsumer.subscribe(kafkaTopic));
  }

  @Override
  public Uni<Void> asyncStop() {
    return Uni.createFrom().voidItem();
  }

}
