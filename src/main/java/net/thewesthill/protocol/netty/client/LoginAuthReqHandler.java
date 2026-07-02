package net.thewesthill.protocol.netty.client;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;

import net.thewesthill.protocol.netty.MessageType;
import net.thewesthill.protocol.netty.struct.Header;
import net.thewesthill.protocol.netty.struct.NettyMessage;

@Slf4j
public class LoginAuthReqHandler extends ChannelInboundHandlerAdapter {

  @Override
  public void channelActive(ChannelHandlerContext ctx) throws Exception {
    ctx.writeAndFlush(buildLoginReq());
  }

  @Override
  public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
    NettyMessage message = (NettyMessage) msg;
    if (message.getHeader() != null
        && message.getHeader().getType() == MessageType.LOGIN_RESP.value()) {
      byte loginResult = (byte) message.getBody();
      if (loginResult != (byte) 0) {
        ctx.close();
      } else {
        log.info("Login is ok : {}", message);
        ctx.fireChannelRead(msg);
      }
    } else {
      ctx.fireChannelRead(msg);
    }
  }

  @Override
  public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
    ctx.fireExceptionCaught(cause);
  }

  private NettyMessage buildLoginReq() {
    NettyMessage message = new NettyMessage();
    Header header = new Header();
    header.setType(MessageType.LOGIN_REQ.value());
    message.setHeader(header);
    return message;
  }
}
