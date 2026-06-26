package net.thewesthill.example.netty.basic;

import java.nio.charset.StandardCharsets;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class NettyTimeClientHandler extends ChannelInboundHandlerAdapter {

  private final ByteBuf firstMessage;

  public NettyTimeClientHandler() {
    byte[] req = "QUERY TIME ORDER".getBytes();
    firstMessage = Unpooled.buffer(req.length);
    firstMessage.writeBytes(req);
  }

  @Override
  public void channelActive(ChannelHandlerContext ctx) {
    ctx.writeAndFlush(firstMessage);
  }

  @Override
  public void channelRead(ChannelHandlerContext ctx, Object msg) {
    ByteBuf buf = (ByteBuf) msg;
    try {
      byte[] req = new byte[buf.readableBytes()];
      buf.readBytes(req);
      String body = new String(req, StandardCharsets.UTF_8);
      log.info("Now is : {}", body);
    } finally {
      buf.release();
    }
  }

  @Override
  public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
    log.info(cause.getMessage());
    ctx.close();
  }
}
