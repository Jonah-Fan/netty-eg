package net.thewesthill.codec.protobuf;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.MultiThreadIoEventLoopGroup;
import io.netty.channel.nio.NioIoHandler;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

import net.thewesthill.codec.pojo.SubscribeReqProto;

public class SubReqServer {

  public static void main(String[] args) {
    int port = 8080;
    if (args != null && args.length > 0) {
      try {
        port = Integer.parseInt(args[0]);
      } catch (NumberFormatException e) {
        e.printStackTrace();
      }
    }
    new SubReqServer().bind(port);
  }

  public void bind(int port) {
    EventLoopGroup bossGroup = new MultiThreadIoEventLoopGroup(NioIoHandler.newFactory());
    EventLoopGroup workGroup = new MultiThreadIoEventLoopGroup(NioIoHandler.newFactory());

    try {
      ServerBootstrap b = new ServerBootstrap();
      b.group(bossGroup, workGroup)
          .channel(NioServerSocketChannel.class)
          .option(ChannelOption.SO_BACKLOG, 100)
          .handler(new LoggingHandler(LogLevel.INFO))
          .childHandler(
              new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel ch) throws Exception {
                  ch.pipeline()
                      .addLast(new ProtobufVarint32FrameDecoder())
                      .addLast(
                          new ProtobufDecoder(SubscribeReqProto.SubscribeReq.getDefaultInstance()))
                      .addLast(new ProtobufVarint32LengthFieldPrepender())
                      .addLast(new ProtobufEncoder())
                      .addLast(new SubReqServerHandler());
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
