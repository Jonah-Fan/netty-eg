package net.thewesthill.protocol.http.xml.server;

import java.util.ArrayList;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpUtil;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.util.CharsetUtil;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import lombok.extern.slf4j.Slf4j;

import net.thewesthill.protocol.http.xml.codec.HttpXmlRequest;
import net.thewesthill.protocol.http.xml.codec.HttpXmlResponse;
import net.thewesthill.protocol.http.xml.pojo.Address;
import net.thewesthill.protocol.http.xml.pojo.Order;

@Slf4j
public class HttpXmlServerHandler extends SimpleChannelInboundHandler<HttpXmlRequest> {

  @Override
  protected void channelRead0(ChannelHandlerContext ctx, HttpXmlRequest msg) throws Exception {
    HttpRequest request = msg.getRequest();
    Order order = (Order) msg.getBody();
    log.info("Http server receive request : {}", order);
    doBusiness(order);
    ChannelFuture future = ctx.writeAndFlush(new HttpXmlResponse(null, order));
    if (!HttpUtil.isKeepAlive(request)) {
      future.addListener(
          new GenericFutureListener<Future<? super Void>>() {
            @Override
            public void operationComplete(Future<? super Void> future) throws Exception {
              ctx.close();
            }
          });
    }
  }

  @Override
  public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
    if (ctx.channel().isActive()) {
      sendError(ctx, HttpResponseStatus.INTERNAL_SERVER_ERROR);
    }
  }

  private void doBusiness(Order order) {
    order.getCustomer().setLastName("Jonheey");
    ArrayList<String> midNames = new ArrayList<String>();
    midNames.add("Bai");
    order.getCustomer().setMiddleName(midNames);
    Address address = order.getBillTo();
    address.setCity("Shenzhen");
    address.setCountry("China");
    address.setState("Small Way");
    address.setPostCode("88657");
    order.setBillTo(address);
    order.setShipTo(address);
  }

  private static void sendError(ChannelHandlerContext ctx, HttpResponseStatus status) {
    FullHttpResponse response =
        new DefaultFullHttpResponse(
            HttpVersion.HTTP_1_1,
            status,
            Unpooled.copiedBuffer("Fail: " + status.toString() + "\r\n", CharsetUtil.UTF_8));
    response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/plain; charset=UTF-8");
    ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
  }
}
