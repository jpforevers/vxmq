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

import io.github.jpforevers.vxmq.assist.ModelConstants;
import io.github.jpforevers.vxmq.service.composite.CompositeService;
import io.github.jpforevers.vxmq.service.session.Session;
import io.github.jpforevers.vxmq.service.session.SessionService;
import io.github.jpforevers.vxmq.shell.ShellCmdConstants;
import io.vertx.core.cli.Option;
import io.vertx.mutiny.core.Vertx;
import io.vertx.mutiny.core.cli.CLI;
import io.vertx.mutiny.core.cli.CommandLine;
import io.vertx.mutiny.ext.shell.command.Command;
import io.vertx.mutiny.ext.shell.command.CommandBuilder;
import io.vertx.mutiny.ext.shell.command.CommandProcess;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class SessionCmdBuilder {

  public static Command build(Vertx vertx, SessionService sessionService, CompositeService compositeService) {
    Option helpOption = new Option().setShortName(ShellCmdConstants.COMMAND_OPTION_HELP_SHORT_NAME).setLongName(ShellCmdConstants.COMMAND_OPTION_HELP_LONG_NAME).setFlag(true)
      .setDescription("Help information");
    Option listOption = new Option().setShortName(ShellCmdConstants.COMMAND_OPTION_LIST_SHORT_NAME).setLongName(ShellCmdConstants.COMMAND_OPTION_LIST_LONG_NAME).setFlag(true)
      .setDescription("List sessions");
    Option countOption = new Option().setShortName(ShellCmdConstants.COMMAND_OPTION_COUNT_SHORT_NAME).setLongName(ShellCmdConstants.COMMAND_OPTION_COUNT_LONG_NAME).setFlag(true)
      .setDescription("Count sessions");
    Option deleteOption = new Option().setShortName(ShellCmdConstants.COMMAND_OPTION_DELETE_SHORT_NAME).setLongName(ShellCmdConstants.COMMAND_OPTION_DELETE_LONG_NAME).setArgName(ShellCmdConstants.COMMAND_ARG_NAME_CLIENT_ID)
      .setDescription("Delete session");
    Option clientOption = new Option().setLongName(ShellCmdConstants.COMMAND_OPTION_CLIENT_LONG_NAME).setArgName(ShellCmdConstants.COMMAND_ARG_NAME_CLIENT_ID)
      .setDescription("Get session of client");
    Option nodeOption = new Option().setLongName(ShellCmdConstants.COMMAND_OPTION_NODE_LONG_NAME).setArgName(ShellCmdConstants.COMMAND_ARG_NAME_NODE_ID)
      .setDescription("Get sessions of node");

    CLI cli = CLI.create(ShellCmdConstants.COMMAND_SESSIONS).setDescription("A command line interface to interact with mqtt session")
      .addOption(helpOption).addOption(listOption).addOption(countOption).addOption(deleteOption).addOption(clientOption).addOption(nodeOption);
    StringBuilder usageBuilder = new StringBuilder();
    cli.getDelegate().usage(usageBuilder);
    CommandBuilder builder = CommandBuilder.command(cli);
    builder.processHandler(process -> {
      CommandLine commandLine = process.commandLine();
      if (commandLine.isFlagEnabled(helpOption.getName())) {
        process.write(usageBuilder.toString()).end();
      } else if (commandLine.isFlagEnabled(countOption.getName())) {
        if (commandLine.isOptionAssigned(clientOption)) {
          sessionService.getSession(commandLine.getRawValueForOption(clientOption))
            .onItem().transform(session -> session != null ? 1 : 0)
            .subscribe().with(count -> process.write(count + "\n").end(), t -> process.write(t.getMessage()).end());
        } else if (commandLine.isOptionAssigned(nodeOption)) {
          sessionService.search(commandLine.getRawValueForOption(nodeOption))
            .onItem().transform(List::size)
            .subscribe().with(count -> process.write(count + "\n").end(), t -> process.write(t.getMessage()).end());
        } else {
          sessionService.count()
            .subscribe().with(count -> process.write(count + "\n").end(), t -> process.write(t.getMessage()).end());
        }
      } else if (commandLine.isFlagEnabled(listOption.getName())) {
        if (commandLine.isOptionAssigned(clientOption)) {
          sessionService.getSession(commandLine.getRawValueForOption(clientOption))
            .subscribe().with(session -> writeSessionsToProcess(session != null ? List.of(session) : new ArrayList<>(), process), t -> process.write(t.getMessage()).end());
        } else if (commandLine.isOptionAssigned(nodeOption)) {
          sessionService.search(commandLine.getRawValueForOption(nodeOption))
            .subscribe().with(sessions -> writeSessionsToProcess(sessions, process), t -> process.write(t.getMessage()).end());
        } else {
          sessionService.allSessions()
            .subscribe().with(sessions -> writeSessionsToProcess(sessions, process), t -> process.write(t.getMessage()).end());
        }
      } else if (commandLine.isOptionAssigned(deleteOption)) {
        compositeService.deleteSession(commandLine.getRawValueForOption(deleteOption))
          .subscribe().with(v -> process.end(), t -> process.write(t.getMessage()).end());
      } else {
        process.write("Command wrong!\n").end();
      }
    });
    return builder.build(vertx);
  }

  private static void writeSessionsToProcess(List<Session> sessions, CommandProcess process) {
    List<String> headers = List.of(ModelConstants.FIELD_NAME_SESSION_ID, ModelConstants.FIELD_NAME_CLIENT_ID, ModelConstants.FIELD_NAME_ONLINE,
      ModelConstants.FIELD_NAME_VERTICLE_ID, ModelConstants.FIELD_NAME_NODE_ID,
      ModelConstants.FIELD_NAME_CLEAN_SESSION, ModelConstants.FIELD_NAME_KEEP_ALIVE,
      ModelConstants.FIELD_NAME_PROTOCOL_LEVEL, ModelConstants.FIELD_NAME_SESSION_EXPIRY_INTERVAL,
      ModelConstants.FIELD_NAME_CREATED_TIME, ModelConstants.FIELD_NAME_UPDATED_TIME);
    List<List<String>> rows = sessions.stream().map(session -> {
      List<String> row = new ArrayList<>();
      row.add(session.getSessionId());
      row.add(session.getClientId());
      row.add(String.valueOf(session.isOnline()));
      row.add(session.getVerticleId());
      row.add(session.getNodeId());
      row.add(String.valueOf(session.isCleanSession()));
      row.add(String.valueOf(session.getKeepAlive()));
      row.add(String.valueOf(session.getProtocolLevel()));
      row.add(String.valueOf(session.getSessionExpiryInterval()));
      row.add(Instant.ofEpochMilli(session.getCreatedTime()).toString());
      row.add(Instant.ofEpochMilli(session.getUpdatedTime()).toString());
      return row;
    }).toList();
    process.write(AsciiTableUtil.format(headers, rows)).write("\n").end();
  }

}
