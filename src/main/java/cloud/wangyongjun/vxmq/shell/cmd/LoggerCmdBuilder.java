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

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import cloud.wangyongjun.vxmq.shell.ShellCmdConstants;
import io.vertx.core.cli.Option;
import io.vertx.mutiny.core.Vertx;
import io.vertx.mutiny.core.cli.CLI;
import io.vertx.mutiny.core.cli.CommandLine;
import io.vertx.mutiny.ext.shell.command.Command;
import io.vertx.mutiny.ext.shell.command.CommandBuilder;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoggerCmdBuilder {

  public static Command build(Vertx vertx) {
    Option helpOption = new Option().setShortName(ShellCmdConstants.COMMAND_OPTION_HELP_SHORT_NAME).setLongName(ShellCmdConstants.COMMAND_OPTION_HELP_LONG_NAME).setFlag(true)
      .setDescription("Help information");
    Option getOption = new Option().setShortName(ShellCmdConstants.COMMAND_OPTION_GET_SHORT_NAME).setLongName(ShellCmdConstants.COMMAND_OPTION_GET_LONG_NAME).setSingleValued(true)
      .setDescription("Get logger level");
    Option setOption = new Option().setShortName(ShellCmdConstants.COMMAND_OPTION_SET_SHORT_NAME).setLongName(ShellCmdConstants.COMMAND_OPTION_SET_LONG_NAME).setSingleValued(true)
      .setDescription("Set logger level, e.g. io.vertx.core=INFO, supported level: TRACE, DEBUG, INFO, WARN, ERROR");
    CLI cli = CLI.create(ShellCmdConstants.COMMAND_LOGGER).setDescription("A command line interface to get or set logger level")
      .addOption(helpOption).addOption(getOption).addOption(setOption);
    StringBuilder usageBuilder = new StringBuilder();
    cli.getDelegate().usage(usageBuilder);
    CommandBuilder builder = CommandBuilder.command(cli);
    builder.processHandler(process -> {
      CommandLine commandLine = process.commandLine();
      if (commandLine.isFlagEnabled(helpOption.getName())) {
        process.write(usageBuilder.toString()).end();
      } else if (commandLine.isOptionAssigned(getOption)) {
        String optionValue = commandLine.getRawValueForOption(getOption);
        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        Level level = loggerContext.getLogger(optionValue).getLevel();
        if (level == null) {
          level = loggerContext.getLogger(Logger.ROOT_LOGGER_NAME).getLevel();
        }
        process.write("Logger level of " + optionValue + " is: " + level.toString() + "\n").end();
      } else if (commandLine.isOptionAssigned(setOption)) {
        String optionValue = commandLine.getRawValueForOption(setOption);
        String logger = StringUtils.substringBefore(optionValue, "=");
        String level = StringUtils.substringAfter(optionValue, "=");
        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        loggerContext.getLogger(logger).setLevel(Level.toLevel(level));
        process.write("Set logger level success!\n").end();
      } else {
        process.write("Command wrong!\n").end();
      }
    });
    return builder.build(vertx);
  }

}
