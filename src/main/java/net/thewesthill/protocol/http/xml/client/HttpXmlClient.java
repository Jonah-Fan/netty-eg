package net.thewesthill.protocol.http.xml.client;

import java.net.InetSocketAddress;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.MultiThreadIoEventLoopGroup;
import io.netty.channel.nio.NioIoHandler;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestEncoder;
import io.netty.handler.codec.http.HttpResponseDecoder;
import lombok.extern.slf4j.Slf4j;

import net.thewesthill.protocol.http.xml.codec.HttpXmlRequestEncoder;
import net.thewesthill.protocol.http.xml.codec.HttpXmlResponseDecoder;
import net.thewesthill.protocol.http.xml.pojo.Order;

@Slf4j
public class HttpXmlClient {

  public static void main(String[] args) {
    int port = 8080;
    if (args != null && args.length > 0) {
      try {
        port = Integer.parseInt(args[0]);
      } catch (NumberFormatException e) {
        log.error("invalid port argument: {}", args[0], e);
      }
    }
    new HttpXmlClient().connect(port);
  }

  public void connect(int port) {
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
                      .addLast("http-decoder", new HttpResponseDecoder())
                      .addLast("http-aggregator", new HttpObjectAggregator(65536))
                      .addLast("xml-decoder", new HttpXmlResponseDecoder(Order.class, true))
                      .addLast("http-encoder", new HttpRequestEncoder())
                      .addLast("xml-encoder", new HttpXmlRequestEncoder())
                      .addLast("xml-client-handler", new HttpXmlClientHandler());
                }
              });
      ChannelFuture f = b.connect(new InetSocketAddress(port)).syncUninterruptibly();
      f.channel().closeFuture().syncUninterruptibly();
    } finally {
      group.shutdownGracefully().syncUninterruptibly();
    }
  }
}
