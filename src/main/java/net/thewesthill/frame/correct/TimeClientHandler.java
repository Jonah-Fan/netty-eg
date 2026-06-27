package net.thewesthill.frame.correct;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TimeClientHandler extends ChannelInboundHandlerAdapter {

  private final byte[] req;
  private int counter;

  public TimeClientHandler() {
    req = ("QUERY TIME ORDER" + System.lineSeparator()).getBytes();
  }

  @Override
  public void channelActive(ChannelHandlerContext ctx) throws Exception {
    ByteBuf buf;
    for (int i = 0; i < 100; i++) {
      buf = Unpooled.buffer(req.length);
      buf.writeBytes(req);
      ctx.writeAndFlush(buf);
    }
  }

  @Override
  public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
    String body = msg.toString();
    log.info("Nos is : {} ; the counter is : {}", body, ++counter);
  }

  @Override
  public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
    log.error("channel exception", cause);
    ctx.close();
  }
}
