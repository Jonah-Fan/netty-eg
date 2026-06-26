package net.thewesthill.protocol.http;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.MultiThreadIoEventLoopGroup;
import io.netty.channel.nio.NioIoHandler;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseDecoder;
import io.netty.handler.stream.ChunkedWriteHandler;

public class HttpFileServer {

  private static final String DEFAULT_URL = "/src/net/thewesthill/netty";

  public void run(final int port, final String url) {
    EventLoopGroup bossGroup = new MultiThreadIoEventLoopGroup(NioIoHandler.newFactory());
    EventLoopGroup workGroup = new MultiThreadIoEventLoopGroup(NioIoHandler.newFactory());

    try {
      ServerBootstrap b = new ServerBootstrap();
      b.group(bossGroup, workGroup)
          .channel(NioServerSocketChannel.class)
          .childHandler(
              new ChannelInitializer<io.netty.channel.socket.SocketChannel>() {
                @Override
                protected void initChannel(io.netty.channel.socket.SocketChannel ch)
                    throws Exception {
                  ch.pipeline()
                      .addLast("http-decoder", new HttpRequestDecoder())
                      .addLast("http-aggregator", new HttpObjectAggregator(65536))
                      .addLast("http-encoder", new HttpResponseDecoder())
                      .addLast("http-chunked", new ChunkedWriteHandler());
                }
              });
      ChannelFuture f = b.bind("192.168.1.102", port).syncUninterruptibly();
      f.channel().closeFuture().syncUninterruptibly();
    } finally {
      bossGroup.shutdownGracefully().syncUninterruptibly();
      workGroup.shutdownGracefully().syncUninterruptibly();
    }
  }
}
