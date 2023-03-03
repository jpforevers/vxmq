package cloud.wangyongjun.vxmq.mqtt.will;

import cloud.wangyongjun.vxmq.assist.Nullable;
import io.smallrye.mutiny.Uni;

import java.util.List;

public interface WillService {

  /**
   * Save or update will.
   *
   * @param will will
   * @return Void
   */
  Uni<Void> saveOrUpdateWill(Will will);

  /**
   * Get will.
   *
   * @param sessionId sessionId
   * @return Will, may be null.
   */
  @Nullable
  Uni<Will> getWill(String sessionId);

  /**
   * Remove will.
   *
   * @param sessionId sessionId
   * @return Void
   */
  Uni<Void> removeWill(String sessionId);

  /**
   * All wills.
   *
   * @return Will list.
   */
  Uni<List<Will>> allWills();

  /**
   * Count will.
   *
   * @return Will count.
   */
  Uni<Long> count();

}
