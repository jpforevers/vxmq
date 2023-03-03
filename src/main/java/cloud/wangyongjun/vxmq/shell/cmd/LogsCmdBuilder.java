package cloud.wangyongjun.vxmq.shell.cmd;

import cloud.wangyongjun.vxmq.assist.Config;
import cloud.wangyongjun.vxmq.shell.ShellCmdConstants;
import io.vertx.core.cli.Option;
import io.vertx.core.json.JsonObject;
import io.vertx.mutiny.core.Vertx;
import io.vertx.mutiny.core.cli.CLI;
import io.vertx.mutiny.core.cli.CommandLine;
import io.vertx.mutiny.ext.shell.command.Command;
import io.vertx.mutiny.ext.shell.command.CommandBuilder;
import io.vertx.mutiny.ext.shell.command.CommandProcess;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

public class LogsCmdBuilder {

  public static Command build(Vertx vertx, JsonObject config) {
    Option helpOption = new Option().setShortName(ShellCmdConstants.COMMAND_OPTION_HELP_SHORT_NAME).setLongName(ShellCmdConstants.COMMAND_OPTION_HELP_LONG_NAME).setFlag(true)
      .setDescription("Help information");
    Option followOption = new Option().setShortName(ShellCmdConstants.COMMAND_OPTION_FOLLOW_SHORT_NAME).setLongName(ShellCmdConstants.COMMAND_OPTION_FOLLOW_LONG_NAME).setFlag(true)
      .setDescription("Follow log output");
    CLI cli = CLI.create(ShellCmdConstants.COMMAND_LOGS).setDescription("A command line interface to show the logs")
      .addOption(helpOption).addOption(followOption);
    StringBuilder usageBuilder = new StringBuilder();
    cli.getDelegate().usage(usageBuilder);
    CommandBuilder builder = CommandBuilder.command(cli);
    builder.processHandler(process -> {
      CommandLine commandLine = process.commandLine();
      if (commandLine.isFlagEnabled(helpOption.getName())) {
        process.write(usageBuilder.toString()).end();
      } else if (commandLine.isFlagEnabled(followOption.getName())) {
        TailLogFileThread tailLogFileThread = new TailLogFileThread(process, config);
        tailLogFileThread.start();
        process.interruptHandler(process::end);
        process.endHandler(() -> tailLogFileThread.setCancelled(true));
      } else {
        process.write("Command wrong!\n").end();
      }
    });
    return builder.build(vertx);
  }

  static class TailLogFileThread extends Thread {

    private final CommandProcess process;
    private final JsonObject config;

    private volatile boolean cancelled = false;

    public TailLogFileThread(CommandProcess process, JsonObject config) {
      this.process = process;
      this.config = config;
    }

    @Override
    public void run() {
      try {
        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(Config.getLogFile(config))));
        String line;
        while (!cancelled) {
          line = reader.readLine();
          if (line == null) {
            Thread.sleep(2000);
          } else {
            process.write(line + "\n");
          }
        }
      } catch (IOException | InterruptedException e) {
        throw new RuntimeException(e);
      }
    }

    public TailLogFileThread setCancelled(boolean cancelled) {
      this.cancelled = cancelled;
      return this;
    }

  }

}
