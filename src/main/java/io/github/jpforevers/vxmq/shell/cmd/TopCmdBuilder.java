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

package io.github.jpforevers.vxmq.shell.cmd;

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
