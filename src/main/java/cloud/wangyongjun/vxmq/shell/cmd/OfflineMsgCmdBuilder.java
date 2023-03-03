package cloud.wangyongjun.vxmq.shell.cmd;

import cloud.wangyongjun.vxmq.shell.ShellCmdConstants;
import cloud.wangyongjun.vxmq.mqtt.msg.MsgService;
import cloud.wangyongjun.vxmq.mqtt.msg.MsgToClient;
import cloud.wangyongjun.vxmq.mqtt.session.SessionService;
import io.smallrye.mutiny.Uni;
import io.vertx.core.cli.Option;
import io.vertx.mutiny.core.Vertx;
import io.vertx.mutiny.core.cli.CLI;
import io.vertx.mutiny.core.cli.CommandLine;
import io.vertx.mutiny.ext.shell.command.Command;
import io.vertx.mutiny.ext.shell.command.CommandBuilder;

import java.util.Collections;

public class OfflineMsgCmdBuilder {

  public static Command build(Vertx vertx, MsgService msgService, SessionService sessionService) {
    Option helpOption = new Option().setShortName(ShellCmdConstants.COMMAND_OPTION_HELP_SHORT_NAME)
      .setLongName(ShellCmdConstants.COMMAND_OPTION_HELP_LONG_NAME).setFlag(true)
      .setDescription("Help information");
    Option listOption = new Option().setShortName(ShellCmdConstants.COMMAND_OPTION_LIST_SHORT_NAME)
      .setLongName(ShellCmdConstants.COMMAND_OPTION_LIST_LONG_NAME).setFlag(true)
      .setDescription("List offlineMsgs");
    Option clientOption = new Option()
      .setLongName(ShellCmdConstants.COMMAND_OPTION_CLIENT_LONG_NAME)
      .setDescription("List offlineMsgs of the client");

    CLI cli = CLI.create(ShellCmdConstants.COMMAND_OFFLINEMSG).setDescription("A command line interface to interact with offlineMsg")
      .addOption(helpOption).addOption(listOption).addOption(clientOption);
    StringBuilder usageBuilder = new StringBuilder();
    cli.getDelegate().usage(usageBuilder);
    CommandBuilder builder = CommandBuilder.command(cli);
    builder.processHandler(process -> {
      CommandLine commandLine = process.commandLine();
      if (commandLine.isFlagEnabled(helpOption.getName())) {
        process.write(usageBuilder.toString()).end();
      } else if (commandLine.isFlagEnabled(listOption.getName()) && commandLine.isOptionAssigned(clientOption)) {
        String clientId = commandLine.getRawValueForOption(clientOption);
        sessionService.getSession(clientId)
          .onItem().transformToUni(session -> {
            if (session != null) {
              return msgService.allOfflineMsgOfSession(session.getSessionId());
            } else {
              return Uni.createFrom().item(Collections.<MsgToClient>emptyList());
            }
          })
          .subscribe().with(offlineMsgs -> {
            offlineMsgs.forEach(offlineMsg -> process.write(offlineMsg.toJson().encode() + "\n"));
            process.end();
          }, t -> process.write(t.getMessage()).end());
      } else {
        process.write("Command wrong!\n").end();
      }
    });
    return builder.build(vertx);
  }

}
