package cloud.wangyongjun.vxmq.http.q.ping;

import cloud.wangyongjun.vxmq.http.AbstractHandler;
import io.smallrye.mutiny.Uni;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.mutiny.core.MultiMap;
import io.vertx.mutiny.core.Vertx;
import io.vertx.mutiny.core.buffer.Buffer;
import io.vertx.mutiny.ext.web.RoutingContext;

import java.util.Map;

public class PingHandler extends AbstractHandler {

  public PingHandler(Vertx vertx) {
    super(vertx);
  }

  @Override
  public Uni<Void> handleRequest(RoutingContext routingContext) {
    JsonObject result = new JsonObject();

    result.put("path", routingContext.request().path());
    result.put("method", routingContext.request().method().name());
    result.put("headers", multiMapToJsonArray(routingContext.request().headers()));

    return Uni.createFrom().voidItem()
      .onItem().transformToUni(v -> routingContext.response().send(Buffer.newInstance(result.toBuffer())));
  }

  private static JsonArray multiMapToJsonArray(MultiMap multiMap) {
    JsonArray jsonArray = new JsonArray();
    for (Map.Entry<String, String> entry : multiMap) {
      JsonObject jsonObject = new JsonObject();
      jsonObject.put("name", entry.getKey());
      jsonObject.put("value", entry.getValue());
      jsonArray.add(jsonObject);
    }
    return jsonArray;
  }

}
