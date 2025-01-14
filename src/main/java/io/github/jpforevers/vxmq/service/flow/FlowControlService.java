package io.github.jpforevers.vxmq.service.flow;

public interface FlowControlService {

  int incrementAndGetInboundReceive(String clientId);

  int decrementAndGetInboundReceive(String clientId);

  void clearInboundReceive(String clientId);

}
