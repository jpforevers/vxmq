package io.github.jpforevers.vxmq.service.flow;

import io.vertx.mutiny.core.Vertx;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentHashMap;

public class FlowServiceImpl implements FlowService{

  private final static Logger LOGGER = LoggerFactory.getLogger(FlowServiceImpl.class);

  private static volatile FlowService flowService;

  public static FlowService getSingleton(Vertx vertx) {
    if (flowService == null) {
      synchronized (FlowServiceImpl.class) {
        if (flowService == null) {
          flowService = new FlowServiceImpl(vertx);
        }
      }
    }
    return flowService;
  }

  private final Vertx vertx;
  private final ConcurrentHashMap<String, Integer> clientIdToInboundReceiveMap;

  private FlowServiceImpl(Vertx vertx) {
    this.vertx = vertx;
    this.clientIdToInboundReceiveMap = new ConcurrentHashMap<>();
  }

  @Override
  public int incrementAndGetInboundReceive(String clientId) {
    int x = clientIdToInboundReceiveMap.compute(clientId, (k, v) -> {
      if (v == null) {
        return 1;
      } else {
        return ++v;
      }
    });
    LOGGER.debug("incrementAndGetInboundReceive: {}, {}", x, clientId);
    return x;
  }

  @Override
  public int decrementAndGetInboundReceive(String clientId) {
    int x = clientIdToInboundReceiveMap.compute(clientId, (k, v) -> {
      if (v == null) {
        return 0;
      } else {
        return --v;
      }
    });
    LOGGER.debug("decrementAndGetInboundReceive: {}, {}", x, clientId);
    return x;
  }

  @Override
  public void clearInboundReceive(String clientId) {
    LOGGER.debug("clearInboundReceive: " + clientId);
    clientIdToInboundReceiveMap.remove(clientId);
  }

}
