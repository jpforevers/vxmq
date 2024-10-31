package io.github.jpforevers.vxmq.shell.cmd;

import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.core.Vertx;
import io.vertx.mutiny.core.net.NetClient;
import io.vertx.mutiny.ext.shell.command.Command;
import io.vertx.mutiny.ext.shell.command.CommandBuilder;

import java.nio.charset.StandardCharsets;

public class TelehackCmdBuilder {

  public static Command build(Vertx vertx) {

    return CommandBuilder.command("telehack")
      .processHandler(process -> {
        // Connect the client
        NetClient client = vertx.createNetClient();
        client.connect(23, "telehack.com")
          .onItem().transformToUni(socket -> {
            // Ctrl-C closes the socket
            process.interruptHandler(socket::closeAndForget);

            process.stdinHandler(socket::writeAndForget);

            socket.handler(buff -> {
              // Push the data to the Shell
              process.write(buff.toString(StandardCharsets.UTF_8));
            });

            socket.exceptionHandler(err -> {
              err.printStackTrace();
              process.write("Error occurred: " + err.getMessage() + "\n").end();
              socket.closeAndForget();
            });

            // When socket closes, end the command
            socket.closeHandler(process::end);

            return Uni.createFrom().voidItem();
          }).subscribe().with(v -> {
          }, t -> {
            process.write("Error occurred when connecting to Telehack: " + t.getMessage() + "\n").end();
          });
      }).build(vertx);
  }

}
