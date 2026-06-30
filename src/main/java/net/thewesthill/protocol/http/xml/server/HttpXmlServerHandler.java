package net.thewesthill.protocol.http.xml.server;

import java.util.ArrayList;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpUtil;
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
}
