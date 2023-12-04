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

import cloud.wangyongjun.vxmq.assist.VertxUtil;
import cloud.wangyongjun.vxmq.shell.ShellCmdConstants;
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
