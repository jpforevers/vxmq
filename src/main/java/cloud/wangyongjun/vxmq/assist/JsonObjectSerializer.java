package cloud.wangyongjun.vxmq.assist;

import io.vertx.core.json.JsonObject;
import org.apache.kafka.common.serialization.Serializer;

public class JsonObjectSerializer implements Serializer<JsonObject> {

  @Override
  public byte[] serialize(String topic, JsonObject data) {
    if (data == null){
      return null;
    }
    return data.toBuffer().getBytes();
  }

}