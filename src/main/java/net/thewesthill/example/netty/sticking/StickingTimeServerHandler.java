package net.thewesthill.example.netty.sticking;

import java.nio.charset.StandardCharsets;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class StickingTimeServerHandler extends ChannelInboundHandlerAdapter {

  private int counter;

  @Override
  public void channelRead(ChannelHandlerContext ctx, Object msg) {
    ByteBuf buf = (ByteBuf) msg;
    try {
      byte[] req = new byte[buf.readableBytes()];
      buf.readBytes(req);
      String body = new String(req, StandardCharsets.UTF_8).substring(0,
          req.length - System.lineSeparator().length());
      log.info("The time server receive order : {} ; the counter is : {}", body, ++counter);
      String currentTime = "QUERY TIME ORDER".equalsIgnoreCase(body) ? new java.util.Date(
          System.currentTimeMillis()).toString() : "BAD ORDER";
      currentTime = currentTime + System.lineSeparator();
      ByteBuf resp = Unpooled.copiedBuffer(currentTime.getBytes());
      ctx.writeAndFlush(resp);
    } finally {
      buf.release();
    }
  }

  @Override
  public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
    ctx.close();
  }
}
