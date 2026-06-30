package net.thewesthill.protocol.http.xml.client;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

import net.thewesthill.protocol.http.xml.codec.HttpXmlRequest;
import net.thewesthill.protocol.http.xml.codec.HttpXmlResponse;
import net.thewesthill.protocol.http.xml.pojo.OrderFactory;

@Slf4j
public class HttpXmlClientHandler extends SimpleChannelInboundHandler<HttpXmlResponse> {

  @Override
  public void channelActive(ChannelHandlerContext ctx) throws Exception {
    HttpXmlRequest request = new HttpXmlRequest(null, OrderFactory.create(666));
    ctx.writeAndFlush(request);
  }

  @Override
  protected void channelRead0(ChannelHandlerContext ctx, HttpXmlResponse msg) throws Exception {
    log.info(
        "The client receive response of http header is : {}", msg.getResponse().headers().names());
    log.info("The client receive response of http body is : {}", msg.getResult());
  }

  @Override
  public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
    log.error("channel exception", cause);
    ctx.close();
  }
}
