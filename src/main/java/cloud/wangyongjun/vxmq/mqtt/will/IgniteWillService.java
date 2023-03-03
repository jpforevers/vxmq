package cloud.wangyongjun.vxmq.mqtt.will;

import cloud.wangyongjun.vxmq.mqtt.IgniteAssist;
import cloud.wangyongjun.vxmq.mqtt.IgniteUtil;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.core.Vertx;
import org.apache.ignite.IgniteCache;
import org.apache.ignite.cache.query.QueryCursor;
import org.apache.ignite.cache.query.ScanQuery;

import javax.cache.Cache;
import java.util.List;
import java.util.stream.Collectors;

public class IgniteWillService implements WillService {

  private static volatile IgniteWillService igniteWillService;

  public static IgniteWillService getSingleton(Vertx vertx) {
    if (igniteWillService == null) {
      synchronized (IgniteWillService.class) {
        if (igniteWillService == null) {
          igniteWillService = new IgniteWillService(vertx);
        }
      }
    }
    return igniteWillService;
  }

  private final IgniteCache<String, Will> willCache;

  private IgniteWillService(Vertx vertx) {
    this.willCache = IgniteAssist.initWillCache(IgniteUtil.getIgnite(vertx));
  }

  @Override
  public Uni<Void> saveOrUpdateWill(Will will) {
    willCache.put(will.getSessionId(), will);
    return Uni.createFrom().voidItem();
  }

  @Override
  public Uni<Will> getWill(String sessionId) {
    return Uni.createFrom().item(willCache.get(sessionId));
  }

  @Override
  public Uni<Void> removeWill(String sessionId) {
    willCache.remove(sessionId);
    return Uni.createFrom().voidItem();
  }

  @Override
  public Uni<List<Will>> allWills() {
    QueryCursor<Cache.Entry<String, Will>> cursor = willCache.query(new ScanQuery<>());
    return Uni.createFrom().item(cursor.getAll().stream().map(Cache.Entry::getValue).collect(Collectors.toList()));
  }

  @Override
  public Uni<Long> count() {
    QueryCursor<String> cursor = willCache.<Cache.Entry<String, Will>, String>query(new ScanQuery<>(), Cache.Entry::getKey);
    return Uni.createFrom().item((long) cursor.getAll().size());
  }

}
