package cloud.wangyongjun.vxmq.mqtt.retain;

import io.smallrye.mutiny.Uni;

import java.util.List;

public interface RetainService {

  Uni<Void> saveOrUpdateRetain(Retain retain);

  Uni<Void> removeRetain(String topicName);

  Uni<List<Retain>> allTopicMatchRetains(String topicFilter);

  Uni<List<Retain>> allRetains();

  Uni<Long> count();

}
