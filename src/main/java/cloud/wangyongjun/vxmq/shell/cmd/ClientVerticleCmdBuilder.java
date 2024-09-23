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

package cloud.wangyongjun.vxmq.shell.cmd;

import cloud.wangyongjun.vxmq.assist.ModelConstants;
import cloud.wangyongjun.vxmq.service.client.ClientService;
import cloud.wangyongjun.vxmq.service.session.Session;
import cloud.wangyongjun.vxmq.service.session.SessionService;
import cloud.wangyongjun.vxmq.shell.ShellCmdConstants;
import io.vertx.core.cli.Option;
import io.vertx.mutiny.core.Vertx;
import io.vertx.mutiny.core.cli.CLI;
import io.vertx.mutiny.core.cli.CommandLine;
import io.vertx.mutiny.ext.shell.command.Command;
import io.vertx.mutiny.ext.shell.command.CommandBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class ClientVerticleCmdBuilder {

  public static Command build(Vertx vertx, ClientService clientService, SessionService sessionService) {
    Option helpOption = new Option().setShortName(ShellCmdConstants.COMMAND_OPTION_HELP_SHORT_NAME).setLongName(ShellCmdConstants.COMMAND_OPTION_HELP_LONG_NAME).setFlag(true)
      .setDescription("Help information");
    Option listOption = new Option().setShortName(ShellCmdConstants.COMMAND_OPTION_LIST_SHORT_NAME).setLongName(ShellCmdConstants.COMMAND_OPTION_LIST_LONG_NAME).setFlag(true)
      .setDescription("List client verticle");
    Option countOption = new Option().setShortName(ShellCmdConstants.COMMAND_OPTION_COUNT_SHORT_NAME).setLongName(ShellCmdConstants.COMMAND_OPTION_COUNT_LONG_NAME).setFlag(true)
      .setDescription("Count client verticle");

    CLI cli = CLI.create(ShellCmdConstants.COMMAND_CLIENT_VERTICLE).setDescription("A command line interface to interact with client verticle")
      .addOption(helpOption).addOption(listOption).addOption(countOption);
    StringBuilder usageBuilder = new StringBuilder();
    cli.getDelegate().usage(usageBuilder);
    CommandBuilder builder = CommandBuilder.command(cli);
    builder.processHandler(process -> {
      CommandLine commandLine = process.commandLine();
      if (commandLine.isFlagEnabled(helpOption.getName())) {
        process.write(usageBuilder.toString()).end();
      } else if (commandLine.isFlagEnabled(countOption.getName())) {
        long count = clientService.verticleIds().size();
        process.write(count + "\n").end();
      } else if (commandLine.isFlagEnabled(listOption.getName())) {
        List<String> verticleIds = clientService.verticleIds();
        sessionService.allSessions()
          .onItem().invoke(sessions -> {
            List<String> headers = List.of(ModelConstants.FIELD_NAME_VERTICLE_ID, ModelConstants.FIELD_NAME_CLIENT_ID);
            List<List<String>> rows = new ArrayList<>();
            for (String verticleId : verticleIds) {
              Optional<Session> sessionOptional = sessions.stream().filter(session -> Objects.equals(verticleId, session.getVerticleId())).findAny();
              String clientId = sessionOptional.map(Session::getClientId).orElse("");
              List<String> row = new ArrayList<>();
              row.add(verticleId);
              row.add(clientId);
              rows.add(row);
            }
            process.write(AsciiTableUtil.format(headers, rows)).write("\n").end();
          })
          .replaceWithVoid()
          .subscribe().with(v -> {}, t -> process.write(t.getMessage()).end());
      } else {
        process.write("Command wrong!\n").end();
      }
    });
    return builder.build(vertx);
  }

}
