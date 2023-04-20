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

import cloud.wangyongjun.vxmq.shell.ShellCmdConstants;
import cloud.wangyongjun.vxmq.service.msg.MsgService;
import io.vertx.core.cli.Option;
import io.vertx.mutiny.core.Vertx;
import io.vertx.mutiny.core.cli.CLI;
import io.vertx.mutiny.core.cli.CommandLine;
import io.vertx.mutiny.ext.shell.command.Command;
import io.vertx.mutiny.ext.shell.command.CommandBuilder;

public class InboundQos2PubCmdBuilder {

  public static Command build(Vertx vertx, MsgService msgService) {
    Option helpOption = new Option().setShortName(ShellCmdConstants.COMMAND_OPTION_HELP_SHORT_NAME)
      .setLongName(ShellCmdConstants.COMMAND_OPTION_HELP_LONG_NAME).setFlag(true)
      .setDescription("Help information");
    Option listOption = new Option().setShortName(ShellCmdConstants.COMMAND_OPTION_LIST_SHORT_NAME)
      .setLongName(ShellCmdConstants.COMMAND_OPTION_LIST_LONG_NAME).setFlag(true)
      .setDescription("List inboundQos2Pubs");
    Option countOption = new Option().setShortName(ShellCmdConstants.COMMAND_OPTION_COUNT_SHORT_NAME)
      .setLongName(ShellCmdConstants.COMMAND_OPTION_COUNT_LONG_NAME).setFlag(true)
      .setDescription("Count inboundQos2Pub");

    CLI cli = CLI.create(ShellCmdConstants.COMMAND_INBOUNDQOS2PUB).setDescription("A command line interface to interact with inboundQos2Pub")
      .addOption(helpOption).addOption(listOption).addOption(countOption);
    StringBuilder usageBuilder = new StringBuilder();
    cli.getDelegate().usage(usageBuilder);
    CommandBuilder builder = CommandBuilder.command(cli);
    builder.processHandler(process -> {
      CommandLine commandLine = process.commandLine();
      if (commandLine.isFlagEnabled(helpOption.getName())) {
        process.write(usageBuilder.toString()).end();
      } else if (commandLine.isFlagEnabled(countOption.getName())) {
        msgService.countInboundQos2Pub()
          .subscribe().with(count -> process.write(count + "\n").end(), t -> process.write(t.getMessage()).end());
      } else if (commandLine.isFlagEnabled(listOption.getName())) {
        msgService.allInboundQos2Pub()
          .subscribe().with(inboundQos2Pubs -> {
            inboundQos2Pubs.forEach(inboundQos2Pub -> process.write(inboundQos2Pub.toJson().encode() + "\n"));
            process.end();
          }, t -> process.write(t.getMessage()).end());
      } else {
        process.write("Command wrong!\n").end();
      }
    });
    return builder.build(vertx);
  }

}
