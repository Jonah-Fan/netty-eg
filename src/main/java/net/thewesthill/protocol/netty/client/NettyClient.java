package net.thewesthill.protocol.netty.client;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.MultiThreadIoEventLoopGroup;
import io.netty.channel.nio.NioIoHandler;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.ReadTimeoutHandler;
import lombok.extern.slf4j.Slf4j;

import net.thewesthill.protocol.netty.NettyConstant;
import net.thewesthill.protocol.netty.codec.NettyMessageDecoder;
import net.thewesthill.protocol.netty.codec.NettyMessageEncoder;

@Slf4j
public class NettyClient {

  public static void main(String[] args) throws InterruptedException {
    new NettyClient().connect(NettyConstant.PORT, NettyConstant.REMOTE_IP);
  }

  private ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
  EventLoopGroup group = new MultiThreadIoEventLoopGroup(NioIoHandler.newFactory());

  public void connect(int port, String host) throws InterruptedException {
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
                      .addLast(new NettyMessageDecoder(1024 * 1024, 4, 4, -8, 0))
                      .addLast("MessageEncoder", new NettyMessageEncoder())
                      .addLast("readTimeoutHandler", new ReadTimeoutHandler(50))
                      .addLast("LoginAuthHandler", new LoginAuthReqHandler())
                      .addLast("HeartBeatHandler", new HeartBeatReqHandler());
                }
              });
      ChannelFuture f = b.connect(host, port).sync();
      f.channel().closeFuture().sync();
    } finally {
      executor.execute(
          new Runnable() {
            @Override
            public void run() {
              try {
                TimeUnit.SECONDS.sleep(5);
                try {
                  connect(NettyConstant.PORT, NettyConstant.REMOTE_IP);
                } catch (Exception e) {
                  log.error("channel connect exception", e);
                }
              } catch (InterruptedException e) {
                log.error("connect exception", e);
              }
            }
          });
    }
  }
}
