package net.thewesthill.basic;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TimeServerHandler extends ChannelInboundHandlerAdapter {

  @Override
  public void channelRead(ChannelHandlerContext ctx, Object msg) {
    ByteBuf buf = (ByteBuf) msg;
    try {
      byte[] reqBytes = new byte[buf.readableBytes()];
      buf.readBytes(reqBytes);
      String body = new String(reqBytes, StandardCharsets.UTF_8);
      log.info("The time server receive order : {}", body);

      String respStr;
      if ("QUERY TIME ORDER".equalsIgnoreCase(body)) {
        respStr = new Date(System.currentTimeMillis()).toString();
      } else {
        respStr = "BAD ORDER";
      }

      ByteBuf respBuf = Unpooled.copiedBuffer(respStr.getBytes(StandardCharsets.UTF_8));
      ctx.write(respBuf);
    } finally {
      buf.release();
    }
  }

  @Override
  public void channelReadComplete(ChannelHandlerContext ctx) {
    ctx.flush();
  }

  @Override
  public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
    ctx.close();
  }
}
