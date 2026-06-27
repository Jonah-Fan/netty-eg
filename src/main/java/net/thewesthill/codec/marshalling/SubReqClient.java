package net.thewesthill.codec.marshalling;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.MultiThreadIoEventLoopGroup;
import io.netty.channel.nio.NioIoHandler;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.extern.slf4j.Slf4j;

import net.thewesthill.codec.protobuf.SubReqClientHandler;

@Slf4j
public class SubReqClient {

  public static void main(String[] args) {
    int port = 8080;
    if (args != null && args.length > 0) {
      try {
        port = Integer.parseInt(args[0]);
      } catch (NumberFormatException e) {
        log.error("invalid port argument: {}", args[0], e);
      }
    }
    new SubReqClient().connect("127.0.0.1", port);
  }

  public void connect(String host, int port) {
    EventLoopGroup group = new MultiThreadIoEventLoopGroup(NioIoHandler.newFactory());

    try {
      Bootstrap b = new Bootstrap();
      b.group(group)
          .channel(NioSocketChannel.class)
          .option(ChannelOption.TCP_NODELAY, true)
          .handler(
              new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel ch) throws Exception {
                  ch.pipeline()
                      .addLast(MarshallingCodecFactory.buildMarshallingDecoder())
                      .addLast(MarshallingCodecFactory.buildMarshallingEncoder())
                      .addLast(new SubReqClientHandler());
                }
              });
      ChannelFuture f = b.connect(host, port).syncUninterruptibly();
      f.channel().closeFuture().syncUninterruptibly();
    } finally {
      group.shutdownGracefully().syncUninterruptibly();
    }
  }
}
