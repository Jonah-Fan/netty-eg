package net.thewesthill.codec.protobuf;

import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;

import net.thewesthill.codec.pojo.SubscribeReqProto;
import net.thewesthill.codec.pojo.SubscribeReqProto.SubscribeReq;
import net.thewesthill.codec.pojo.SubscribeRespProto;

@Slf4j
@Sharable
public class SubReqServerHandler extends ChannelInboundHandlerAdapter {

  @Override
  public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
    SubscribeReqProto.SubscribeReq req = (SubscribeReq) msg;
    if ("Jonah".equalsIgnoreCase(req.getUserName())) {
      log.info("Service accept client subscribe req : [{}]", req.toString());
      ctx.writeAndFlush(resp(req.getSubReqID()));
    }
  }

  @Override
  public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
    cause.printStackTrace();
    ctx.close();
  }

  private SubscribeRespProto.SubscribeResp resp(int subReqId) {
    SubscribeRespProto.SubscribeResp.Builder builder =
        SubscribeRespProto.SubscribeResp.newBuilder();
    builder.setSubReqID(subReqId);
    builder.setRespCode(0);
    builder.setDesc("Netty book order succeed, 3 days later, send to the designated address");
    return builder.build();
  }
}
