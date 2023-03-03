package cloud.wangyongjun.vxmq.mqtt.session;

import cloud.wangyongjun.vxmq.assist.Nullable;
import io.smallrye.mutiny.Uni;

import java.util.List;

public interface SessionService {

  @Nullable
  Uni<Session> getSession(String clientId);

  Uni<Void> saveOrUpdateSession(Session session);

  Uni<Void> updateLatestUpdatedTime(String clientId, long time);

  Uni<Void> removeSession(String clientId);

  Uni<List<Session>> allSessions();

  Uni<Long> count();

}
