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

package cloud.wangyongjun.vxmq.shell;

import cloud.wangyongjun.vxmq.assist.Config;
import cloud.wangyongjun.vxmq.service.ServiceFactory;
import cloud.wangyongjun.vxmq.service.client.ClientService;
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
    SessionService sessionService = ServiceFactory.sessionService(vertx);
    WillService willService = ServiceFactory.willService(vertx);
    SubService subService = ServiceFactory.subService(vertx);
    MsgService msgService = ServiceFactory.msgService(vertx);
    CompositeService compositeService = ServiceFactory.compositeService(vertx);
    ClientService clientService = ServiceFactory.clientService(vertx);

    CommandRegistry commandRegistry = CommandRegistry.getShared(vertx);
    String banner = vertx.fileSystem().readFileBlocking("banner.txt").toString(StandardCharsets.UTF_8);

    SSHTermOptions sshTermOptions = new SSHTermOptions();
    sshTermOptions.setPort(Config.getShellServerPort());
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
      .onItem().transformToUni(v -> commandRegistry.registerCommand(SessionCmdBuilder.build(vertx, sessionService, compositeService)))
      .onItem().transformToUni(v -> commandRegistry.registerCommand(WillCmdBuilder.build(vertx, willService)))
      .onItem().transformToUni(v -> commandRegistry.registerCommand(SubsCmdBuilder.build(vertx, subService)))
      .onItem().transformToUni(v -> commandRegistry.registerCommand(ClientVerticleCmdBuilder.build(vertx, clientService, sessionService)))
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
