package net.thewesthill.basic;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.MultiThreadIoEventLoopGroup;
import io.netty.channel.nio.NioIoHandler;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.extern.slf4j.Slf4j;

import net.thewesthill.frame.fault.TimeServerHandler;

@Slf4j
public class TimeServer {

  public static void main(String[] args) {
    int port = 8080;
    if (args != null && args.length > 0) {
      try {
        port = Integer.parseInt(args[0]);
      } catch (NumberFormatException e) {
        log.error("invalid port argument: {}", args[0], e);
      }
    }
    new TimeServer().bind(port);
  }

  public void bind(int port) {
    EventLoopGroup bossGroup = new MultiThreadIoEventLoopGroup(NioIoHandler.newFactory());
    EventLoopGroup workGroup = new MultiThreadIoEventLoopGroup(NioIoHandler.newFactory());

    try {
      ServerBootstrap b = new ServerBootstrap();
      b.group(bossGroup, workGroup)
          .channel(NioServerSocketChannel.class)
          .option(ChannelOption.SO_BACKLOG, 1024)
          .childHandler(
              new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel ch) {
                  ch.pipeline().addLast(new TimeServerHandler());
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
