package io.github.jpforevers.vxmq.assist;

import io.github.jpforevers.vxmq.service.client.ToClientVerticleMsg;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.MessageCodec;
import io.vertx.core.json.JsonObject;
import io.vertx.mutiny.core.Vertx;
import io.vertx.mutiny.core.eventbus.EventBus;

public class EBFactory {

  public static void init(Vertx vertx) {
    EventBus eventBus = vertx.eventBus();
    eventBus.getDelegate().registerDefaultCodec(ToClientVerticleMsg.class, new ToClientVerticleMsgCodec());
  }

  public enum EBServices {

    NOTHING_SERVICE(EBAddress.SERVICE_NOTHING_SERVICE),
    SUB_SERVICE(EBAddress.SERVICE_SUB_SERVICE),
    AUTHENTICATION_SERVICE(EBAddress.SERVICE_AUTHENTICATION_SERVICE);

    private final String ebAddress;

    EBServices(String ebAddress) {
      this.ebAddress = ebAddress;
    }

    public String getEbAddress() {
      return ebAddress;
    }

  }

  public static class EBAddress {

    public static final String SERVICE_NOTHING_SERVICE = "service.NothingService";
    public static final String SERVICE_SUB_SERVICE = "service.SubService";
    public static final String SERVICE_AUTHENTICATION_SERVICE = "service.AuthenticationService";

    public static final String EVENT_NOTHING = "event.nothing";
    public static final String EVENT_MQTT_CONNECTED = "event.mqtt.connected";
    public static final String EVENT_MQTT_SESSION_TAKEN_OVER = "event.mqtt.session.taken-over";
    public static final String EVENT_MQTT_CONNECT_FAILED = "event.mqtt.connect.failed";
    public static final String EVENT_MQTT_PROTOCOL_ERROR = "event.mqtt.protocol.error";
    public static final String EVENT_MQTT_ENDPOINT_CLOSED = "event.mqtt.endpoint-closed";
    public static final String EVENT_MQTT_DISCONNECTED = "event.mqtt.disconnected";
    public static final String EVENT_MQTT_PING = "event.mqtt.ping";
    public static final String EVENT_MQTT_SUBSCRIBED = "event.mqtt.subscribed";
    public static final String EVENT_MQTT_UNSUBSCRIBED = "event.mqtt.unsubscribed";
    public static final String EVENT_MQTT_PUBLISH_INBOUND_ACCEPTED = "event.mqtt.publish.inbound.accepted";
    public static final String EVENT_MQTT_PUBLISH_OUTBOUND_ACKED = "event.mqtt.publish.outbound.acked";

  }

  public static class ToClientVerticleMsgCodec implements MessageCodec<ToClientVerticleMsg, ToClientVerticleMsg> {

    @Override
    public void encodeToWire(Buffer buffer, ToClientVerticleMsg toClientVerticleMsg) {
      Buffer encoded = toClientVerticleMsg.toJson().toBuffer();
      buffer.appendInt(encoded.length());
      buffer.appendBuffer(encoded);
    }

    @Override
    public ToClientVerticleMsg decodeFromWire(int pos, Buffer buffer) {
      int length = buffer.getInt(pos);
      pos += 4;
      return new ToClientVerticleMsg(new JsonObject(buffer.slice(pos, pos + length)));
    }

    @Override
    public ToClientVerticleMsg transform(ToClientVerticleMsg toClientVerticleMsg) {
      return toClientVerticleMsg;
    }

    @Override
    public String name() {
      return ToClientVerticleMsgCodec.class.getSimpleName();
    }

    @Override
    public byte systemCodecID() {
      return -1;
    }

  }

}
