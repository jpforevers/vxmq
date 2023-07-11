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

package cloud.wangyongjun.vxmq.shell.cmd;

import io.vertx.mutiny.core.Vertx;
import io.vertx.mutiny.ext.shell.command.Command;
import io.vertx.mutiny.ext.shell.command.CommandBuilder;

import java.util.ArrayList;
import java.util.Formatter;
import java.util.List;

public class TopCmdBuilder {

  public static Command build(Vertx vertx) {
    return CommandBuilder.command("top")
      .processHandler(process -> {
        long id = vertx.setPeriodic(500, id_ -> {
          StringBuilder buf = new StringBuilder();
          Formatter formatter = new Formatter(buf);
          List<Thread> threads = new ArrayList<>(Thread.getAllStackTraces().keySet());
          for (int i = 1; i <= process.height(); i++) {
            // Change cursor position and erase line with ANSI escape code magic
            buf.append("\033[").append(i).append(";1H\033[K");
            String format = "  %1$-5s %2$-20s %3$-50s %4$s";
            if (i == 1) {
              formatter.format(format,
                "ID",
                "STATE",
                "NAME",
                "GROUP");
            } else {
              int index = i - 2;
              if (index < threads.size()) {
                Thread thread = threads.get(index);
                formatter.format(format,
                  thread.getId(),
                  thread.getState().name(),
                  thread.getName(),
                  thread.getThreadGroup().getName());
              }
            }
          }
          process.write(buf.toString());
        });
        // Terminate when user hits Ctrl-C
        process.interruptHandler(() -> {
          vertx.cancelTimer(id);
          process.end();
        });
      }).build(vertx);
  }

}
