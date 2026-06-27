package net.thewesthill.frame.delimiter;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class EchoServerHandler extends ChannelInboundHandlerAdapter {

  int counter;

  @Override
  public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
    String body = msg.toString();
    log.info("This is {} time receive client : [{}]", ++counter, body);
    body += "$_";
    ByteBuf echo = Unpooled.copiedBuffer(body.getBytes());
    ctx.writeAndFlush(echo);
  }

  @Override
  public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
    log.error("channel exception", cause);
    ctx.close();
  }
}
