package net.thewesthill.protobuf;

import java.util.ArrayList;
import java.util.List;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SubReqClientHandler extends ChannelInboundHandlerAdapter {

  @Override
  public void channelActive(ChannelHandlerContext ctx) throws Exception {
    for (int i = 0; i < 10; i++) {
      ctx.write(subReq(i));
    }
    ctx.flush();
  }

  @Override
  public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
    log.info("Receive server response : [{}]", msg);
  }

  @Override
  public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
    ctx.flush();
  }

  @Override
  public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
    cause.printStackTrace();
    ctx.close();
  }

  private SubscribeReqProto.SubscribeReq subReq(int i) {
    SubscribeReqProto.SubscribeReq.Builder builder = SubscribeReqProto.SubscribeReq.newBuilder();
    builder.setSubReqID(i);
    builder.setUserName("Jonah");
    builder.setProductName("Netty Book For Protobuf");
    List<String> address = new ArrayList<>();
    address.add("NanJing YuHuaTai");
    address.add("BeiJing LiuLiChang");
    address.add("ShenZhen HongShuLin");
    builder.addAllAddress(address);
    return builder.build();
  }
}
