package net.thewesthill.protocol.udp;

import java.net.InetSocketAddress;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.MultiThreadIoEventLoopGroup;
import io.netty.channel.nio.NioIoHandler;
import io.netty.channel.socket.DatagramPacket;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.util.CharsetUtil;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ChineseProverbClient {

  public static void main(String[] args) {
    int port = 8080;
    if (args != null && args.length > 0) {
      try {
        port = Integer.parseInt(args[0]);
      } catch (NumberFormatException e) {
        log.error("invalid port argument: {}", port, e);
      }
    }
    new ChineseProverbClient().run(port);
  }

  public void run(int port) {
    EventLoopGroup group = new MultiThreadIoEventLoopGroup(NioIoHandler.newFactory());

    try {
      Bootstrap b = new Bootstrap();
      b.group(group)
          .channel(NioDatagramChannel.class)
          .option(ChannelOption.SO_BROADCAST, true)
          .handler(new ChineseProverbClientHandler());
      Channel ch = b.bind(0).syncUninterruptibly().channel();
      ch.writeAndFlush(
              new DatagramPacket(
                  Unpooled.copiedBuffer("Proverb dictionary query?", CharsetUtil.UTF_8),
                  new InetSocketAddress("255.255.255.255", port)))
          .syncUninterruptibly();
      if (!ch.closeFuture().awaitUninterruptibly(15000)) {
        log.info("Query TimeOut.");
      }
    } finally {
      group.shutdownGracefully().syncUninterruptibly();
    }
  }
}
