package cloud.wangyongjun.vxmq;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.mutiny.core.json.pointer.JsonPointer;

public class Main {

  public static void main(String[] args) {
    JsonObject jsonObject = new JsonObject();
    jsonObject.put("a", new JsonObject().put("b", new JsonArray().add("b1").add("b2").add("b3")));
    JsonPointer pointer1 = JsonPointer.create().append("a").append("b");
    JsonPointer pointer2 = JsonPointer.create().append("a").append("b").append(1);
    Object o1 = pointer1.queryJson(jsonObject);
    Object o2 = pointer2.queryJson(jsonObject);
    System.out.println(o1.getClass());
    System.out.println(o1);
    System.out.println(o2.getClass());
    System.out.println(o2);

  }
}
