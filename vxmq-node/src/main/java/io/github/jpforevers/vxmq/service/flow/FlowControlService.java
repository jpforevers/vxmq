package io.github.jpforevers.vxmq.service.flow;

public interface FlowControlService {

  int incrementAndGetInboundReceive(String clientId);

  void decrementInboundReceive(String clientId);

  void clearInboundReceive(String clientId);

  int getAndIncrementOutboundReceive(String clientId);

  void decrementOutboundReceive(String clientId);

  void clearOutboundReceive(String clientId);

}
