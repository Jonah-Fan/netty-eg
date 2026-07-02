package net.thewesthill.protocol.netty.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.MultiThreadIoEventLoopGroup;
import io.netty.channel.nio.NioIoHandler;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.ReadTimeoutHandler;
import lombok.extern.slf4j.Slf4j;

import net.thewesthill.protocol.netty.NettyConstant;
import net.thewesthill.protocol.netty.codec.NettyMessageDecoder;
import net.thewesthill.protocol.netty.codec.NettyMessageEncoder;

@Slf4j
public class NettyServer {

  public static void main(String[] args) throws InterruptedException {
    new NettyServer().bind();
  }

  public void bind() throws InterruptedException {
    EventLoopGroup bossGroup = new MultiThreadIoEventLoopGroup(NioIoHandler.newFactory());
    EventLoopGroup workerGroup = new MultiThreadIoEventLoopGroup(NioIoHandler.newFactory());
    ServerBootstrap b = new ServerBootstrap();
    b.group(bossGroup, workerGroup)
        .channel(NioServerSocketChannel.class)
        .option(ChannelOption.SO_BACKLOG, 100)
        .handler(new LoggingHandler(LogLevel.INFO))
        .childHandler(
            new ChannelInitializer<SocketChannel>() {
              @Override
              protected void initChannel(SocketChannel ch) throws Exception {
                ch.pipeline()
                    .addLast(new NettyMessageDecoder(1024 * 1024, 4, 4, -8, 0))
                    .addLast(new NettyMessageEncoder())
                    .addLast("readTimeoutHandler", new ReadTimeoutHandler(50))
                    .addLast(new LoginAuthRespHandler())
                    .addLast("HeartBeatHandler", new HeartBeatRespHandler());
              }
            });
    b.bind(NettyConstant.REMOTE_IP, NettyConstant.PORT).sync();
    log.info("Netty server start ok : {} : {}", NettyConstant.REMOTE_IP, NettyConstant.PORT);
  }
}
