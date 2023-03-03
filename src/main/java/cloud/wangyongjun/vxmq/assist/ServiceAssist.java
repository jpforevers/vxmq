package cloud.wangyongjun.vxmq.assist;

import cloud.wangyongjun.vxmq.event.DefaultEventService;
import cloud.wangyongjun.vxmq.event.EventService;
import cloud.wangyongjun.vxmq.mqtt.client.ClientService;
import cloud.wangyongjun.vxmq.mqtt.client.DefaultClientService;
import cloud.wangyongjun.vxmq.mqtt.composite.CompositeService;
import cloud.wangyongjun.vxmq.mqtt.composite.DefaultCompositeService;
import cloud.wangyongjun.vxmq.mqtt.msg.IgniteMsgService;
import cloud.wangyongjun.vxmq.mqtt.msg.MsgService;
import cloud.wangyongjun.vxmq.mqtt.retain.IgniteRetainService;
import cloud.wangyongjun.vxmq.mqtt.retain.RetainService;
import cloud.wangyongjun.vxmq.mqtt.session.IgniteSessionService;
import cloud.wangyongjun.vxmq.mqtt.session.SessionService;
import cloud.wangyongjun.vxmq.mqtt.sub.mutiny.SubService;
import cloud.wangyongjun.vxmq.mqtt.will.IgniteWillService;
import cloud.wangyongjun.vxmq.mqtt.will.WillService;
import io.vertx.core.json.JsonObject;
import io.vertx.mutiny.core.Vertx;

public class ServiceAssist {

  public static SessionService sessionService(Vertx vertx) {
    return IgniteSessionService.getInstance(vertx);
  }

  public static SubService subService(Vertx vertx) {
    return VertxServiceProxyBuilder.buildSubService(vertx);
  }

  public static ClientService clientService(Vertx vertx) {
    return DefaultClientService.getInstance(vertx);
  }

  public static MsgService msgService(Vertx vertx, JsonObject config) {
    return IgniteMsgService.getInstance(vertx, config);
  }

  public static RetainService retainService(Vertx vertx) {
    return IgniteRetainService.getInstance(vertx);
  }

  public static WillService willService(Vertx vertx) {
    return IgniteWillService.getInstance(vertx);
  }

  public static CompositeService compositeService(Vertx vertx, JsonObject config) {
    return DefaultCompositeService.getInstance(vertx, config, sessionService(vertx), subService(vertx), willService(vertx), msgService(vertx, config), retainService(vertx), clientService(vertx));
  }

  public static EventService eventService(Vertx vertx){
    return DefaultEventService.getInstance(vertx);
  }

}
