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
