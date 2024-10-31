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

import io.github.jpforevers.vxmq.assist.VertxUtil;
import io.github.jpforevers.vxmq.shell.ShellCmdConstants;
import io.vertx.core.cli.Option;
import io.vertx.core.spi.cluster.NodeInfo;
import io.vertx.mutiny.core.Vertx;
import io.vertx.mutiny.core.cli.CLI;
import io.vertx.mutiny.core.cli.CommandLine;
import io.vertx.mutiny.ext.shell.command.Command;
import io.vertx.mutiny.ext.shell.command.CommandBuilder;

import java.util.ArrayList;
import java.util.List;

public class ClusterCmdBuilder {

  public static Command build(Vertx vertx) {
    Option helpOption = new Option().setShortName(ShellCmdConstants.COMMAND_OPTION_HELP_SHORT_NAME).setLongName(ShellCmdConstants.COMMAND_OPTION_HELP_LONG_NAME).setFlag(true)
      .setDescription("Help information");
    Option listOption = new Option().setShortName(ShellCmdConstants.COMMAND_OPTION_LIST_SHORT_NAME).setLongName(ShellCmdConstants.COMMAND_OPTION_LIST_LONG_NAME).setFlag(true)
      .setDescription("List sessions");

    CLI cli = CLI.create(ShellCmdConstants.COMMAND_CLUSTER).setDescription("A command line interface to interact with cluster nodes")
      .addOption(helpOption).addOption(listOption);
    StringBuilder usageBuilder = new StringBuilder();
    cli.getDelegate().usage(usageBuilder);
    CommandBuilder builder = CommandBuilder.command(cli);
    builder.processHandler(process -> {
      CommandLine commandLine = process.commandLine();
      if (commandLine.isFlagEnabled(helpOption.getName())) {
        process.write(usageBuilder.toString()).end();
      } else if (commandLine.isFlagEnabled(listOption.getName())) {
        List<String> headers = List.of("current", "nodeId", "host", "port", "metadata");
        List<List<String>> rows = new ArrayList<>();
        String currentNodeId = VertxUtil.getNodeId(vertx);
        for (String nodeId : VertxUtil.getNodes(vertx)) {
          List<String> row = new ArrayList<>(headers.size());
          row.add(currentNodeId.equals(nodeId) ? "*" : "");
          row.add(nodeId);
          NodeInfo nodeInfo = VertxUtil.getNodeInfo(vertx, nodeId);
          row.add(nodeInfo.host());
          row.add(String.valueOf(nodeInfo.port()));
          row.add(nodeInfo.metadata() != null ? nodeInfo.metadata().encode() : "");
          rows.add(row);
        }
        process.write(AsciiTableUtil.format(headers, rows)).write("\n").end();
      } else {
        process.write("Command wrong!\n").end();
      }
    });
    return builder.build(vertx);
  }

}
