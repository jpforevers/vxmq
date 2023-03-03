package cloud.wangyongjun.vxmq.shell;

import cloud.wangyongjun.vxmq.assist.Config;
import cloud.wangyongjun.vxmq.assist.ServiceAssist;
import cloud.wangyongjun.vxmq.shell.cmd.*;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.vertx.core.AbstractVerticle;
import io.vertx.ext.shell.ShellServiceOptions;
import io.vertx.ext.shell.term.TelnetTermOptions;
import io.vertx.mutiny.ext.shell.ShellService;
import io.vertx.mutiny.ext.shell.command.CommandRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;

public class ShellServerVerticle extends AbstractVerticle {

  private final static Logger LOGGER = LoggerFactory.getLogger(ShellServerVerticle.class);

  @Override
  public Uni<Void> asyncStart() {
    CommandRegistry commandRegistry = CommandRegistry.getShared(vertx);
    String banner = vertx.fileSystem().readFileBlocking("banner.txt").toString(StandardCharsets.UTF_8);
    TelnetTermOptions telnetTermOptions = new TelnetTermOptions().setPort(Config.getShellServerPort(config()));
    ShellServiceOptions shellServiceOptions = new ShellServiceOptions().setWelcomeMessage(banner).setTelnetOptions(telnetTermOptions);
    ShellService shellService = ShellService.create(vertx, shellServiceOptions);
    return Uni.createFrom().voidItem()
      .onItem().transformToUni(v -> commandRegistry.registerCommand(LoggerCmdBuilder.build(vertx)))
      .onItem().transformToUni(v -> commandRegistry.registerCommand(LogsCmdBuilder.build(vertx, config())))
      .onItem().transformToUni(v -> commandRegistry.registerCommand(SessionCmdBuilder.build(vertx, ServiceAssist.sessionService(vertx))))
      .onItem().transformToUni(v -> commandRegistry.registerCommand(WillCmdBuilder.build(vertx, ServiceAssist.willService(vertx))))
      .onItem().transformToUni(v -> commandRegistry.registerCommand(SubCmdBuilder.build(vertx, ServiceAssist.subService(vertx))))
      .onItem().transformToUni(v -> commandRegistry.registerCommand(ClientVerticleCmdBuilder.build(vertx)))
      .onItem().transformToUni(v -> commandRegistry.registerCommand(InboundQos2PubCmdBuilder.build(vertx, ServiceAssist.msgService(vertx, config()))))
      .onItem().transformToUni(v -> commandRegistry.registerCommand(OutboundQos1PubCmdBuilder.build(vertx, ServiceAssist.msgService(vertx, config()))))
      .onItem().transformToUni(v -> commandRegistry.registerCommand(OutboundQos2PubCmdBuilder.build(vertx, ServiceAssist.msgService(vertx, config()))))
      .onItem().transformToUni(v -> commandRegistry.registerCommand(OutboundQos2RelCmdBuilder.build(vertx, ServiceAssist.msgService(vertx, config()))))
      .onItem().transformToUni(v -> commandRegistry.registerCommand(OfflineMsgCmdBuilder.build(vertx, ServiceAssist.msgService(vertx, config()), ServiceAssist.sessionService(vertx))))
      .onItem().transformToUni(v -> shellService.start())
      .replaceWithVoid();
  }

  @Override
  public Uni<Void> asyncStop() {
    return Uni.createFrom().voidItem();
  }

}
