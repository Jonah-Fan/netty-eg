package net.thewesthill.example.netty.codec.msgpack;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioIoHandler;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MsgPackEchoServer {

  private final int port;

  public MsgPackEchoServer(int port) {
    this.port = port;
  }

  public static void main(String[] args) {
    int port = 8080;

    if (args != null && args.length > 0) {
      try {
        port = Integer.parseInt(args[0]);
      } catch (NumberFormatException e) {
        log.info(e.getMessage());
      }
    }
    new MsgPackEchoServer(port).run();
  }

  public void run() {
    EventLoopGroup bossGroup = new MultiThreadIoEventLoopGroup(NioIoHandler.newFactory());
    EventLoopGroup workGroup = new MultiThreadIoEventLoopGroup(NioIoHandler.newFactory());

    try {
      ServerBootstrap b = new ServerBootstrap();
      b.group(bossGroup, workGroup).channel(NioServerSocketChannel.class)
          .option(ChannelOption.SO_BACKLOG, 1024)
          .handler(new LoggingHandler(LogLevel.INFO))
          .childHandler(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel ch) throws Exception {
              ch.pipeline()
                  // process frame decoder in bound.
                  .addLast("frame decoder", new LengthFieldBasedFrameDecoder(65535, 0, 2, 0, 2))
                  .addLast("msgpack decoder", new MsgPackDecoder())
                  // process frame encoder out bound.
                  .addLast("frame encoder", new LengthFieldPrepender(2))
                  .addLast("msgpack encoder", new MsgPackEncoder())
                  .addLast(new MsgPackServerHandler());
            }
          });
      ChannelFuture f = b.bind(port).syncUninterruptibly();
      f.channel().closeFuture().syncUninterruptibly();
    } finally {
      bossGroup.shutdownGracefully().syncUninterruptibly();
      workGroup.shutdownGracefully().syncUninterruptibly();
    }
  }
}
