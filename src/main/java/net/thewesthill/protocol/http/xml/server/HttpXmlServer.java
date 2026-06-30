package net.thewesthill.protocol.http.xml.server;

import java.net.InetSocketAddress;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.MultiThreadIoEventLoopGroup;
import io.netty.channel.nio.NioIoHandler;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import lombok.extern.slf4j.Slf4j;

import net.thewesthill.protocol.http.xml.codec.HttpXmlRequestDecoder;
import net.thewesthill.protocol.http.xml.codec.HttpXmlResponseEncoder;
import net.thewesthill.protocol.http.xml.pojo.Order;

@Slf4j
public class HttpXmlServer {

  public static void main(String[] args) {
    int port = 8080;
    if (args != null && args.length > 0) {
      try {
        port = Integer.parseInt(args[0]);
      } catch (NumberFormatException e) {
        log.error("invalid port argument: {}", port, e);
      }
    }
    new HttpXmlServer().run(port);
  }

  private void run(final int port) {
    EventLoopGroup bossGroup = new MultiThreadIoEventLoopGroup(NioIoHandler.newFactory());
    EventLoopGroup workGroup = new MultiThreadIoEventLoopGroup(NioIoHandler.newFactory());

    try {
      ServerBootstrap b = new ServerBootstrap();
      b.group(bossGroup, workGroup)
          .channel(NioServerSocketChannel.class)
          .handler(new LoggingHandler(LogLevel.INFO))
          .childHandler(
              new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel ch) throws Exception {
                  ch.pipeline()
                      .addLast("http-decoder", new HttpRequestDecoder())
                      .addLast("http-aggregator", new HttpObjectAggregator(65536))
                      .addLast("xml-decoder", new HttpXmlRequestDecoder(Order.class, true))
                      .addLast("http-encoder", new HttpResponseEncoder())
                      .addLast("xml-encoder", new HttpXmlResponseEncoder())
                      .addLast("xml-server-handler", new HttpXmlServerHandler());
                }
              });
      ChannelFuture f = b.bind(new InetSocketAddress(port)).syncUninterruptibly();
      f.channel().closeFuture().syncUninterruptibly();
    } finally {
      bossGroup.shutdownGracefully().syncUninterruptibly();
      workGroup.shutdownGracefully().syncUninterruptibly();
    }
  }
}
