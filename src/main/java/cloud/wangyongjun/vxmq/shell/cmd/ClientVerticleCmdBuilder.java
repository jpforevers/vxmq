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

import cloud.wangyongjun.vxmq.assist.ModelConstants;
import cloud.wangyongjun.vxmq.service.client.ClientService;
import cloud.wangyongjun.vxmq.service.session.Session;
import cloud.wangyongjun.vxmq.service.session.SessionService;
import cloud.wangyongjun.vxmq.shell.ShellCmdConstants;
import cloud.wangyongjun.vxmq.service.client.ClientVerticle;
import io.vertx.core.cli.Option;
import io.vertx.core.impl.VertxInternal;
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
        VertxInternal vertxInternal = (VertxInternal) vertx.getDelegate();
        long count = vertx.deploymentIDs().stream().filter(id -> vertxInternal.getDeployment(id).verticleIdentifier().contains(ClientVerticle.class.getSimpleName())).count();
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
