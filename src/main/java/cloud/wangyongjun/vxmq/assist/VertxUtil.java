/*
 * Copyright (C) 2023-2024 王用军
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package cloud.wangyongjun.vxmq.assist;

import io.smallrye.mutiny.vertx.UniHelper;
import io.vertx.core.Promise;
import io.vertx.core.impl.VertxInternal;
import io.vertx.core.spi.cluster.NodeInfo;
import io.vertx.mutiny.core.Vertx;

import java.time.Duration;
import java.util.List;

public class VertxUtil {

  public static VertxInternal getVertxInternal(Vertx vertx) {
    return ((VertxInternal) vertx.getDelegate());
  }

  public static List<String> getNodes(Vertx vertx) {
    return VertxUtil.getVertxInternal(vertx).getClusterManager().getNodes();
  }

  public static String getNodeId(Vertx vertx) {
    return VertxUtil.getVertxInternal(vertx).getClusterManager().getNodeId();
  }

  public static NodeInfo getNodeInfo(Vertx vertx, String nodeId) {
    Promise<NodeInfo> promise = Promise.promise();
    VertxUtil.getVertxInternal(vertx).getClusterManager().getNodeInfo(nodeId, promise);
    return UniHelper.toUni(promise.future()).await().atMost(Duration.ofSeconds(10));
  }

}
