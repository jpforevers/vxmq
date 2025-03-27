package io.github.jpforevers.vxmq.mqtt;

import io.github.jpforevers.vxmq.assist.ConsumerUtil;
import io.github.jpforevers.vxmq.service.ServiceFactory;
import io.github.jpforevers.vxmq.service.client.ClientService;
import io.github.jpforevers.vxmq.service.session.Session;
import io.github.jpforevers.vxmq.service.session.SessionService;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.vertx.core.AbstractVerticle;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * When the client uses the same clientId for duplicate connections, VXMQ allows subsequent connection to be established and the previous connection to be removed. If the frequency of duplicate connections is high, the using of distributed locks in establishing new connection and closing existed connection may cause the previous ClientVerticle to not be properly undeployed, resulting in these dirty ClientVerticles remaining in memory. So create this Verticle, regularly check all ClientVerticles, and clear those invalid ClientVerticles.
 */
public class DirtyClientVerticleCleaner extends AbstractVerticle {

  private static final Logger LOGGER = LoggerFactory.getLogger(DirtyClientVerticleCleaner.class);

  private ClientService clientService;
  private SessionService sessionService;

  @Override
  public Uni<Void> asyncStart() {
    clientService = ServiceFactory.clientService(vertx);
    sessionService = ServiceFactory.sessionService(vertx);

    vertx.setPeriodic(60 * 1000L, l -> {
      vertx.executeBlocking(sessionService.allSessions()
        .onItem().invoke(sessions -> {
          List<String> verticleIds = clientService.verticleIds();
          for (String verticleId : verticleIds) {
            Optional<Session> sessionOptional = sessions.stream().filter(session -> Objects.equals(verticleId, session.getVerticleId())).findAny();
            if (sessionOptional.isEmpty()) {
              sessionService.getSessionByVerticleId(verticleId)
                .onItem().transformToUni(session -> {
                  if (session == null) {
                    LOGGER.warn("Undeploy client verticle without having related session: {}", verticleId);
                    return vertx.undeploy(verticleId);
                  } else {
                    return Uni.createFrom().voidItem();
                  }
                })
                .subscribe().with(ConsumerUtil.nothingToDo(), t -> LOGGER.error("Error occurred when try to undeploy client verticle", t));
            }
          }
        }), true)
        .subscribe().with(v -> {}, Throwable::printStackTrace);
    });
    return Uni.createFrom().voidItem();
  }

  @Override
  public Uni<Void> asyncStop() {
    return Uni.createFrom().voidItem();
  }

}
