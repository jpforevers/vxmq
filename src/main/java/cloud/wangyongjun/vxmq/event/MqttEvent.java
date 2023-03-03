package cloud.wangyongjun.vxmq.event;

public interface MqttEvent extends Event {

  String getClientId();

}
