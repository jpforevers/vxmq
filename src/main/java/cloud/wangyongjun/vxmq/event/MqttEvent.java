package cloud.wangyongjun.vxmq.event;

public interface MqttEvent extends Event {

  default boolean isLocal(){
    return false;
  }

  String getClientId();

}
