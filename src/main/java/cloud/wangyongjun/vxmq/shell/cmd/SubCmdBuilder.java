package cloud.wangyongjun.vxmq.shell.cmd;

import cloud.wangyongjun.vxmq.shell.ShellCmdConstants;
import cloud.wangyongjun.vxmq.mqtt.TopicUtil;
import cloud.wangyongjun.vxmq.mqtt.sub.Subscription;
import cloud.wangyongjun.vxmq.mqtt.sub.mutiny.SubService;
import io.vertx.core.cli.Option;
import io.vertx.mutiny.core.Vertx;
import io.vertx.mutiny.core.cli.CLI;
import io.vertx.mutiny.core.cli.CommandLine;
import io.vertx.mutiny.ext.shell.command.Command;
import io.vertx.mutiny.ext.shell.command.CommandBuilder;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class SubCmdBuilder {

  public static Command build(Vertx vertx, SubService subService) {
    Option helpOption = new Option().setShortName(ShellCmdConstants.COMMAND_OPTION_HELP_SHORT_NAME)
      .setLongName(ShellCmdConstants.COMMAND_OPTION_HELP_LONG_NAME).setFlag(true)
      .setDescription("Help information");
    Option listOption = new Option().setShortName(ShellCmdConstants.COMMAND_OPTION_LIST_SHORT_NAME)
      .setLongName(ShellCmdConstants.COMMAND_OPTION_LIST_LONG_NAME).setFlag(true)
      .setDescription("List subscriptions");
    Option countOption = new Option().setShortName(ShellCmdConstants.COMMAND_OPTION_COUNT_SHORT_NAME)
      .setLongName(ShellCmdConstants.COMMAND_OPTION_COUNT_LONG_NAME).setFlag(true)
      .setDescription("Count subscriptions");
    Option exactOption = new Option().setLongName(ShellCmdConstants.COMMAND_OPTION_EXACT_LONG_NAME).setFlag(true)
      .setDescription("List exact subscriptions");
    Option wildcardOption = new Option().setLongName(ShellCmdConstants.COMMAND_OPTION_WILDCARD_LONG_NAME).setFlag(true)
      .setDescription("List wildcard subscriptions");
    Option matchOption = new Option().setLongName(ShellCmdConstants.COMMAND_OPTION_MATCH_LONG_NAME)
      .setDescription("List match topic subscription");

    CLI cli = CLI.create(ShellCmdConstants.COMMAND_SUB).setDescription("A command line interface to interact with mqtt subscription")
      .addOption(helpOption).addOption(listOption).addOption(countOption).addOption(exactOption).addOption(wildcardOption).addOption(matchOption);
    StringBuilder usageBuilder = new StringBuilder();
    cli.getDelegate().usage(usageBuilder);
    CommandBuilder builder = CommandBuilder.command(cli);
    builder.processHandler(process -> {
      CommandLine commandLine = process.commandLine();
      if (commandLine.isFlagEnabled(helpOption.getName())) {
        process.write(usageBuilder.toString()).end();
      } else if (commandLine.isFlagEnabled(countOption.getName())) {
        if (commandLine.isOptionAssigned(matchOption)) {
          String topicName = commandLine.getRawValueForOption(matchOption);
          if (!TopicUtil.isValidTopicToPublish(topicName)) {
            process.write("Not a topic name!\n").end();
          } else {
            if (commandLine.isFlagEnabled(exactOption.getName())) {
              subService.countMatchExactSubs(topicName)
                .subscribe().with(count -> process.write(count + "\n").end(), t -> process.write(t.getMessage()).end());
            } else if (commandLine.isFlagEnabled(ShellCmdConstants.COMMAND_OPTION_WILDCARD_LONG_NAME)) {
              subService.countMatchWildcardSubs(topicName, false)
                .subscribe().with(count -> process.write(count + "\n").end(), t -> process.write(t.getMessage()).end());
            } else {
              subService.countMatch(topicName, false)
                .subscribe().with(count -> process.write(count + "\n").end(), t -> process.write(t.getMessage()).end());
            }
          }
        } else {
          if (commandLine.isFlagEnabled(ShellCmdConstants.COMMAND_OPTION_EXACT_LONG_NAME)) {
            subService.countExactSubs()
              .subscribe().with(count -> process.write(count + "\n").end(), t -> process.write(t.getMessage()).end());
          } else if (commandLine.isFlagEnabled(wildcardOption.getName())) {
            subService.countWildcardSubs()
              .subscribe().with(count -> process.write(count + "\n").end(), t -> process.write(t.getMessage()).end());
          } else {
            subService.count()
              .subscribe().with(count -> process.write(count + "\n").end(), t -> process.write(t.getMessage()).end());
          }
        }
      } else if (commandLine.isFlagEnabled(ShellCmdConstants.COMMAND_OPTION_LIST_SHORT_NAME)) {
        if (commandLine.isOptionAssigned(matchOption)) {
          String topicName = commandLine.getRawValueForOption(matchOption);
          if (!TopicUtil.isValidTopicToPublish(topicName)) {
            process.write("Not a topic name!\n").end();
          } else {
            if (commandLine.isFlagEnabled(ShellCmdConstants.COMMAND_OPTION_EXACT_LONG_NAME)) {
              subService.allMatchExactSubs(topicName)
                .subscribe().with(subs -> process.write(format(subs)).write("\n").end(), t -> process.write(t.getMessage()).end());
            } else if (commandLine.isFlagEnabled(ShellCmdConstants.COMMAND_OPTION_WILDCARD_LONG_NAME)) {
              subService.allMatchWildcardSubs(topicName, false)
                .subscribe().with(subs -> process.write(format(subs)).write("\n").end(), t -> process.write(t.getMessage()).end());
            } else {
              subService.allMatchSubs(topicName, false)
                .subscribe().with(subs -> process.write(format(subs)).write("\n").end(), t -> process.write(t.getMessage()).end());
            }
          }
        } else {
          if (commandLine.isFlagEnabled(ShellCmdConstants.COMMAND_OPTION_EXACT_LONG_NAME)) {
            subService.allExactSubs()
              .subscribe().with(subs -> process.write(format(subs)).write("\n").end(), t -> process.write(t.getMessage()).end());
          } else if (commandLine.isFlagEnabled(ShellCmdConstants.COMMAND_OPTION_WILDCARD_LONG_NAME)) {
            subService.allWildcardSubs()
              .subscribe().with(subs -> process.write(format(subs)).write("\n").end(), t -> process.write(t.getMessage()).end());
          } else {
            subService.allSubs()
              .subscribe().with(subs -> process.write(format(subs)).write("\n").end(), t -> process.write(t.getMessage()).end());
          }
        }
      } else {
        process.write("Command wrong!\n").end();
      }
    });
    return builder.build(vertx);
  }

  private static String format(List<Subscription> subscriptions) {
    List<String> headers = List.of("sessionId", "clientId", "topicFilter", "qos", "createdTime");
    List<List<String>> rows = subscriptions.stream().map(subscription -> {
      List<String> list = new ArrayList<>();
      list.add(subscription.getSessionId());
      list.add(subscription.getClientId());
      list.add(subscription.getTopicFilter());
      list.add(String.valueOf(subscription.getQos()));
      list.add(Instant.ofEpochMilli(subscription.getCreatedTime()).toString());
      return list;
    }).toList();
    return AsciiTableUtil.format(headers, rows);
  }

}