package io.github.jpforevers.vxmq.service.flow;

import io.vertx.mutiny.core.Vertx;

import java.util.concurrent.ConcurrentHashMap;

public class FlowControlServiceImpl implements FlowControlService {

  private static volatile FlowControlService flowControlService;

  public static FlowControlService getSingleton(Vertx vertx) {
    if (flowControlService == null) {
      synchronized (FlowControlServiceImpl.class) {
        if (flowControlService == null) {
          flowControlService = new FlowControlServiceImpl(vertx);
        }
      }
    }
    return flowControlService;
  }

  private final Vertx vertx;
  private final ConcurrentHashMap<String, Integer> clientIdToInboundReceiveMap;

  private FlowControlServiceImpl(Vertx vertx) {
    this.vertx = vertx;
    this.clientIdToInboundReceiveMap = new ConcurrentHashMap<>();
  }

  @Override
  public int incrementAndGetInboundReceive(String clientId) {
    return clientIdToInboundReceiveMap.compute(clientId, (k, v) -> {
      if (v == null) {
        return 1;
      } else {
        return ++v;
      }
    });
  }

  @Override
  public int decrementAndGetInboundReceive(String clientId) {
    return clientIdToInboundReceiveMap.compute(clientId, (k, v) -> {
      if (v == null) {
        return 0;
      } else {
        return --v < 0 ? 0 : v;
      }
    });
  }

  @Override
  public void clearInboundReceive(String clientId) {
    clientIdToInboundReceiveMap.remove(clientId);
  }

}
