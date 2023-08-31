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
import cloud.wangyongjun.vxmq.service.composite.CompositeService;
import cloud.wangyongjun.vxmq.service.msg.MsgService;
import cloud.wangyongjun.vxmq.service.session.SessionService;
import cloud.wangyongjun.vxmq.service.sub.mutiny.SubService;
import cloud.wangyongjun.vxmq.service.will.WillService;
import cloud.wangyongjun.vxmq.shell.cmd.*;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.vertx.core.AbstractVerticle;
import io.vertx.core.json.JsonObject;
import io.vertx.core.net.JksOptions;
import io.vertx.ext.shell.ShellServiceOptions;
import io.vertx.ext.shell.term.SSHTermOptions;
import io.vertx.mutiny.ext.shell.ShellService;
import io.vertx.mutiny.ext.shell.command.CommandRegistry;

import java.nio.charset.StandardCharsets;

public class ShellServerVerticle extends AbstractVerticle {

  @Override
  public Uni<Void> asyncStart() {
    SessionService sessionService = ServiceFactory.sessionService(vertx, config());
    WillService willService = ServiceFactory.willService(vertx, config());
    SubService subService = ServiceFactory.subService(vertx);
    MsgService msgService = ServiceFactory.msgService(vertx, config());
    CompositeService compositeService = ServiceFactory.compositeService(vertx, config());

    CommandRegistry commandRegistry = CommandRegistry.getShared(vertx);
    String banner = vertx.fileSystem().readFileBlocking("banner.txt").toString(StandardCharsets.UTF_8);

    SSHTermOptions sshTermOptions = new SSHTermOptions();
    sshTermOptions.setPort(Config.getShellServerPort(config()));
    sshTermOptions.setKeyPairOptions(new JksOptions().
      setPath("shell-ssh.jks").
      setPassword("123456"));
    sshTermOptions.setAuthOptions(new JsonObject()
      .put("provider", "properties")
      .put("config", new JsonObject()
        .put("file", "shell-ssh-auth.properties")));

    ShellServiceOptions shellServiceOptions = new ShellServiceOptions().setWelcomeMessage(banner).setSSHOptions(sshTermOptions);
    ShellService shellService = ShellService.create(vertx, shellServiceOptions);
    return Uni.createFrom().voidItem()
      .onItem().transformToUni(v -> commandRegistry.registerCommand(ClusterCmdBuilder.build(vertx)))
      .onItem().transformToUni(v -> commandRegistry.registerCommand(TopCmdBuilder.build(vertx)))
      .onItem().transformToUni(v -> commandRegistry.registerCommand(TelehackCmdBuilder.build(vertx)))
      .onItem().transformToUni(v -> commandRegistry.registerCommand(LoggerCmdBuilder.build(vertx)))
      .onItem().transformToUni(v -> commandRegistry.registerCommand(LogsCmdBuilder.build(vertx, config())))
      .onItem().transformToUni(v -> commandRegistry.registerCommand(SessionCmdBuilder.build(vertx, sessionService, compositeService)))
      .onItem().transformToUni(v -> commandRegistry.registerCommand(WillCmdBuilder.build(vertx, willService)))
      .onItem().transformToUni(v -> commandRegistry.registerCommand(SubsCmdBuilder.build(vertx, subService)))
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
