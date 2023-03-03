package cloud.wangyongjun.vxmq.shell.cmd;

import cloud.wangyongjun.vxmq.shell.ShellCmdConstants;
import cloud.wangyongjun.vxmq.mqtt.session.SessionService;
import io.vertx.core.cli.Option;
import io.vertx.mutiny.core.Vertx;
import io.vertx.mutiny.core.cli.CLI;
import io.vertx.mutiny.core.cli.CommandLine;
import io.vertx.mutiny.ext.shell.command.Command;
import io.vertx.mutiny.ext.shell.command.CommandBuilder;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class SessionCmdBuilder {

  public static Command build(Vertx vertx, SessionService sessionService) {
    Option helpOption = new Option().setShortName(ShellCmdConstants.COMMAND_OPTION_HELP_SHORT_NAME).setLongName(ShellCmdConstants.COMMAND_OPTION_HELP_LONG_NAME).setFlag(true)
      .setDescription("Help information");
    Option listOption = new Option().setShortName(ShellCmdConstants.COMMAND_OPTION_LIST_SHORT_NAME).setLongName(ShellCmdConstants.COMMAND_OPTION_LIST_LONG_NAME).setFlag(true)
      .setDescription("List sessions");
    Option countOption = new Option().setShortName(ShellCmdConstants.COMMAND_OPTION_COUNT_SHORT_NAME).setLongName(ShellCmdConstants.COMMAND_OPTION_COUNT_LONG_NAME).setFlag(true)
      .setDescription("Count sessions");

    CLI cli = CLI.create(ShellCmdConstants.COMMAND_SESSION).setDescription("A command line interface to interact with mqtt session")
      .addOption(helpOption).addOption(listOption).addOption(countOption);
    StringBuilder usageBuilder = new StringBuilder();
    cli.getDelegate().usage(usageBuilder);
    CommandBuilder builder = CommandBuilder.command(cli);
    builder.processHandler(process -> {
      CommandLine commandLine = process.commandLine();
      if (commandLine.isFlagEnabled(helpOption.getName())) {
        process.write(usageBuilder.toString()).end();
      } else if (commandLine.isFlagEnabled(countOption.getName())) {
        sessionService.count()
          .subscribe().with(count -> process.write(count + "\n").end(), t -> process.write(t.getMessage()).end());
      } else if (commandLine.isFlagEnabled(listOption.getName())) {
        sessionService.allSessions()
          .subscribe().with(sessions -> {
            List<String> headers = List.of("sessionId", "clientId", "online", "verticleId", "nodeId", "cleanSession", "protocolLevel", "sessionExpiryInterval", "createdTime", "updatedTime");
            List<List<String>> rows = sessions.stream().map(session -> {
              List<String> list = new ArrayList<>();
              list.add(session.getSessionId());
              list.add(session.getClientId());
              list.add(String.valueOf(session.isOnline()));
              list.add(session.getVerticleId());
              list.add(session.getNodeId());
              list.add(String.valueOf(session.isCleanSession()));
              list.add(String.valueOf(session.getProtocolLevel()));
              list.add(String.valueOf(session.getSessionExpiryInterval()));
              list.add(Instant.ofEpochMilli(session.getCreatedTime()).toString());
              list.add(Instant.ofEpochMilli(session.getUpdatedTime()).toString());
              return list;
            }).toList();
            process.write(AsciiTableUtil.format(headers, rows)).write("\n").end();
          }, t -> process.write(t.getMessage()).end());
      } else {
        process.write("Command wrong!\n").end();
      }
    });
    return builder.build(vertx);
  }

}
