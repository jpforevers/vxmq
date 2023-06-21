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

package cloud.wangyongjun.vxmq.shell;

import cloud.wangyongjun.vxmq.assist.Config;
import cloud.wangyongjun.vxmq.assist.ServiceFactory;
import cloud.wangyongjun.vxmq.service.msg.MsgService;
import cloud.wangyongjun.vxmq.service.session.SessionService;
import cloud.wangyongjun.vxmq.service.sub.mutiny.SubService;
import cloud.wangyongjun.vxmq.service.will.WillService;
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
    SessionService sessionService = ServiceFactory.sessionService(vertx);
    WillService willService = ServiceFactory.willService(vertx);
    SubService subService = ServiceFactory.subService(vertx);
    MsgService msgService = ServiceFactory.msgService(vertx, config());

    CommandRegistry commandRegistry = CommandRegistry.getShared(vertx);
    String banner = vertx.fileSystem().readFileBlocking("banner.txt").toString(StandardCharsets.UTF_8);
    TelnetTermOptions telnetTermOptions = new TelnetTermOptions().setPort(Config.getShellServerPort(config()));
    ShellServiceOptions shellServiceOptions = new ShellServiceOptions().setWelcomeMessage(banner).setTelnetOptions(telnetTermOptions);
    ShellService shellService = ShellService.create(vertx, shellServiceOptions);
    return Uni.createFrom().voidItem()
      .onItem().transformToUni(v -> commandRegistry.registerCommand(LoggerCmdBuilder.build(vertx)))
      .onItem().transformToUni(v -> commandRegistry.registerCommand(LogsCmdBuilder.build(vertx, config())))
      .onItem().transformToUni(v -> commandRegistry.registerCommand(SessionCmdBuilder.build(vertx, sessionService)))
      .onItem().transformToUni(v -> commandRegistry.registerCommand(WillCmdBuilder.build(vertx, willService)))
      .onItem().transformToUni(v -> commandRegistry.registerCommand(SubCmdBuilder.build(vertx, subService)))
      .onItem().transformToUni(v -> commandRegistry.registerCommand(ClientVerticleCmdBuilder.build(vertx)))
      .onItem().transformToUni(v -> commandRegistry.registerCommand(InboundQos2PubCmdBuilder.build(vertx, msgService)))
      .onItem().transformToUni(v -> commandRegistry.registerCommand(OutboundQos1PubCmdBuilder.build(vertx, msgService)))
      .onItem().transformToUni(v -> commandRegistry.registerCommand(OutboundQos2PubCmdBuilder.build(vertx, msgService)))
      .onItem().transformToUni(v -> commandRegistry.registerCommand(OutboundQos2RelCmdBuilder.build(vertx, msgService)))
      .onItem().transformToUni(v -> commandRegistry.registerCommand(OfflineMsgCmdBuilder.build(vertx, msgService, sessionService)))
      .onItem().transformToUni(v -> shellService.start())
      .replaceWithVoid();
  }

  @Override
  public Uni<Void> asyncStop() {
    return Uni.createFrom().voidItem();
  }

}
