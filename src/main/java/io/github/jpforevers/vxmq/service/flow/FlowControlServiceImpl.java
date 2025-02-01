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
  private final ConcurrentHashMap<String, Integer> clientIdToOutboundReceiveMap;

  private FlowControlServiceImpl(Vertx vertx) {
    this.vertx = vertx;
    this.clientIdToInboundReceiveMap = new ConcurrentHashMap<>();
    this.clientIdToOutboundReceiveMap = new ConcurrentHashMap<>();
  }

  @Override
  public int incrementAndGetInboundReceive(String clientId) {
    return clientIdToInboundReceiveMap.compute(clientId, (k, v) -> v == null ? 1 : v + 1);
  }

  @Override
  public void decrementInboundReceive(String clientId) {
    clientIdToInboundReceiveMap.compute(clientId, (k, v) -> v == null ? 0 : Math.max(--v , 0));
  }

  @Override
  public void clearInboundReceive(String clientId) {
    clientIdToInboundReceiveMap.remove(clientId);
  }

  @Override
  public int getAndIncrementOutboundReceive(String clientId) {
    return clientIdToOutboundReceiveMap.compute(clientId, (k, v) -> v == null ? 1 : v + 1) - 1;
  }

  @Override
  public void decrementOutboundReceive(String clientId) {
    clientIdToOutboundReceiveMap.compute(clientId, (k, v) -> v == null ? 0 : Math.max(--v , 0));
  }

  @Override
  public void clearOutboundReceive(String clientId) {
    clientIdToOutboundReceiveMap.remove(clientId);
  }

}
