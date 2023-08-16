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
