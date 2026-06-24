package net.thewesthill.example.netty.sticking;

import java.nio.charset.StandardCharsets;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class StickingTimeClientHandler extends ChannelInboundHandlerAdapter {

  private final byte[] req;
  private int counter;

  public StickingTimeClientHandler() {
    req = ("QUERY TIME ORDER" + System.lineSeparator()).getBytes();
  }

  @Override
  public void channelActive(ChannelHandlerContext ctx) {
    ByteBuf message;
    // send more msg to server. sticking eg.
    for (int i = 0; i < 100; i++) {
      message = Unpooled.buffer(req.length);
      message.writeBytes(req);
      ctx.writeAndFlush(message);
    }
  }

  @Override
  public void channelRead(ChannelHandlerContext ctx, Object msg) {
    ByteBuf buf = (ByteBuf) msg;
    try {
      byte[] req = new byte[buf.readableBytes()];
      buf.readBytes(req);
      String body = new String(req, StandardCharsets.UTF_8);
      log.info("Now is : {} ; the counter is : {}", body, ++counter);
    } finally {
      buf.release();
    }
  }

  @Override
  public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
    log.info(cause.getMessage());
    ctx.close();
  }
}
