package net.thewesthill.netty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioIoHandler;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

public class TimeServer {

    public static void main(String[] args) {
        int port = 8080;
        if (args != null && args.length > 0) {
            try {
                port = Integer.parseInt(args[0]);
            } catch (NumberFormatException ignored) {

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
                    .childHandler(new ChannelInitializer<SocketChannel>() {
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
