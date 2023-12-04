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

import io.vertx.mutiny.core.Vertx;
import io.vertx.spi.cluster.ignite.IgniteClusterManager;
import org.apache.ignite.Ignite;

public class IgniteUtil {

  public static Ignite getIgnite(Vertx vertx) {
    return ((IgniteClusterManager) (VertxUtil.getVertxInternal(vertx).getClusterManager())).getIgniteInstance();
  }

}
