/*
 * Copyright 2018-present 王用军
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package cloud.wangyongjun.vxmq.mqtt;

import cloud.wangyongjun.vxmq.assist.ConsumerUtil;
import cloud.wangyongjun.vxmq.assist.ServiceFactory;
import cloud.wangyongjun.vxmq.service.client.ClientService;
import cloud.wangyongjun.vxmq.service.session.Session;
import cloud.wangyongjun.vxmq.service.session.SessionService;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.vertx.core.AbstractVerticle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class SessionCheckerVerticle extends AbstractVerticle {

  private final static Logger LOGGER = LoggerFactory.getLogger(SessionCheckerVerticle.class);

  public static final String SESSION_CHECKER_LOCK_NAME = "SESSION_CHECKER";

  private SessionService sessionService;
  private ClientService clientService;

  @Override
  public Uni<Void> asyncStart() {
    sessionService = ServiceFactory.sessionService(vertx, config());
    clientService = ServiceFactory.clientService(vertx);

    vertx.setPeriodic(10000, 60000, l -> {
      vertx.sharedData().getLock(SESSION_CHECKER_LOCK_NAME)
        .onItem().call(lock -> sessionService.allSessions()
          .onItem().transformToUni(sessions -> {
            List<Uni<Void>> unis = new ArrayList<>();
            for (Session session : sessions) {
              if ((Instant.now().toEpochMilli() - session.getUpdatedTime()) <= (session.getKeepAlive() * 1000L)){
                continue;
              }
              LOGGER.info("Session checker kick off the session: {}, because the interval between the current time and the last update time of the session exceeds keepalive", session);
              Uni<Void> uni = clientService.closeMqttEndpoint(session.getVerticleId());
              unis.add(uni);
            }
            return Uni.combine().all().unis(unis).collectFailures().discardItems();
          }))
        .onItemOrFailure().invoke((lock, t) -> {
          lock.release();
          if (t != null){
            LOGGER.error("Error occurred when executing session check", t);
          }
        })
        .subscribe().with(ConsumerUtil.nothingToDo(), ConsumerUtil.nothingToDo());
    });
    return Uni.createFrom().voidItem();
  }

  @Override
  public Uni<Void> asyncStop() {
    return Uni.createFrom().voidItem();
  }

}
