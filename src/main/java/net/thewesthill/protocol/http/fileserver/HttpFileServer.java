package net.thewesthill.protocol.http.fileserver;

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
import io.netty.handler.stream.ChunkedWriteHandler;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class HttpFileServer {

  public static void main(String[] args) {
    int port = 8080;
    if (args != null && args.length > 0) {
      try {
        port = Integer.parseInt(args[0]);
      } catch (NumberFormatException e) {
        log.error("invalid port argument: {}", args[0], e);
      }
    }
    new HttpFileServer().run(port, DEFAULT_URL);
  }

  private static final String DEFAULT_URL = "/src";

  public void run(final int port, final String url) {
    EventLoopGroup bossGroup = new MultiThreadIoEventLoopGroup(NioIoHandler.newFactory());
    EventLoopGroup workGroup = new MultiThreadIoEventLoopGroup(NioIoHandler.newFactory());

    try {
      ServerBootstrap b = new ServerBootstrap();
      b.group(bossGroup, workGroup)
          .channel(NioServerSocketChannel.class)
          .childHandler(
              new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel ch)
                    throws Exception {
                  ch.pipeline()
                      .addLast("http-decoder", new HttpRequestDecoder())
                      .addLast("http-aggregator", new HttpObjectAggregator(65536))
                      .addLast("http-encoder", new HttpResponseEncoder())
                      .addLast("http-chunked", new ChunkedWriteHandler())
                      .addLast("fileServerHandler", new HttpFileServerHandler(url));
                }
              });
      ChannelFuture f = b.bind("127.0.0.1", port).syncUninterruptibly();
      f.channel().closeFuture().syncUninterruptibly();
    } finally {
      bossGroup.shutdownGracefully().syncUninterruptibly();
      workGroup.shutdownGracefully().syncUninterruptibly();
    }
  }
}
