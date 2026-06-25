package net.thewesthill.example.netty.codec.msgpack;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.MultiThreadIoEventLoopGroup;
import io.netty.channel.nio.NioIoHandler;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MsgPackEchoClient {

  public static void main(String[] args) {
    int port = 8080;
    if (args != null && args.length > 0) {
      try {
        port = Integer.parseInt(args[0]);
      } catch (NumberFormatException e) {
        log.info(e.getMessage());
      }
    }
    new MsgPackEchoClient("127.0.0.1", port, 1000).run();
  }

  private final String host;
  private final int port;
  private final int sendNumber;

  public MsgPackEchoClient(String host, int port, int sendNumber) {
    this.host = host;
    this.port = port;
    this.sendNumber = sendNumber;
  }

  public void run() {
    EventLoopGroup group = new MultiThreadIoEventLoopGroup(NioIoHandler.newFactory());

    try {
      Bootstrap b = new Bootstrap();
      b.group(group).channel(NioSocketChannel.class)
          .option(ChannelOption.TCP_NODELAY, true)
          .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 3000)
          .handler(new LoggingHandler(LogLevel.INFO))
          .handler(new ChannelInitializer<SocketChannel>() {
            @Override
            public void initChannel(SocketChannel ch) throws Exception {
              ch.pipeline().addLast("msgPack decoder", new MsgPackDecoder())
                  .addLast("msgPack encoder", new MsgPackEncoder())
                  // sendNumber is loop count.
                  .addLast(new MsgPackEchoClientHandler(sendNumber));
            }
          });
      ChannelFuture f = b.connect(host, port).syncUninterruptibly();
      f.channel().close().syncUninterruptibly();
    } finally {
      group.shutdownGracefully().syncUninterruptibly();
    }
  }
}
