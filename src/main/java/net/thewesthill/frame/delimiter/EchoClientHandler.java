package net.thewesthill.frame.delimiter;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class EchoClientHandler extends ChannelInboundHandlerAdapter {

  static final String ECHO_REQ = "Hi, Jonah. Welcome to Netty.$_";
  private int counter;

  @Override
  public void channelActive(ChannelHandlerContext ctx) throws Exception {
    for (int i = 0; i < 100; i++) {
      ctx.writeAndFlush(Unpooled.copiedBuffer(ECHO_REQ.getBytes()));
    }
  }

  @Override
  public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
    log.info("This is {} times receive server : [{}]", ++counter, msg);
  }

  @Override
  public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
    log.info(cause.getMessage());
    ctx.close();
  }
}
