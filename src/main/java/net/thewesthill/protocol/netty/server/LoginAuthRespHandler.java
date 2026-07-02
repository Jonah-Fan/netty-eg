package net.thewesthill.protocol.netty.server;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;

import net.thewesthill.protocol.netty.MessageType;
import net.thewesthill.protocol.netty.struct.Header;
import net.thewesthill.protocol.netty.struct.NettyMessage;

@Slf4j
public class LoginAuthRespHandler extends ChannelInboundHandlerAdapter {

  private Map<String, Boolean> nodeCheck = new ConcurrentHashMap<>();
  private String[] whiteList = {"127.0.0.1", "192.168.1.104"};

  @Override
  public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
    NettyMessage message = (NettyMessage) msg;
    if (message.getHeader() != null
        && message.getHeader().getType() == MessageType.LOGIN_REQ.value()) {
      String nodeIndex = ctx.channel().remoteAddress().toString();
      NettyMessage loginResp;

      if (nodeCheck.containsKey(nodeIndex)) {
        loginResp = buildResponse((byte) -1);
      } else {
        InetSocketAddress address = (InetSocketAddress) ctx.channel().remoteAddress();
        String ip = address.getAddress().getHostAddress();
        boolean isOk = false;
        for (String wip : whiteList) {
          if (wip.equals(ip)) {
            isOk = true;
            break;
          }
        }
        loginResp = isOk ? buildResponse((byte) 0) : buildResponse((byte) -1);
        if (isOk) {
          nodeCheck.put(nodeIndex, true);
        }
      }
      log.info("The login response is : {} body[{}]", loginResp, loginResp.getBody());
      ctx.writeAndFlush(loginResp);
    } else {
      ctx.fireChannelRead(msg);
    }
  }

  @Override
  public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
    nodeCheck.remove(ctx.channel().remoteAddress().toString());
    ctx.close();
    ctx.fireExceptionCaught(cause);
  }

  private NettyMessage buildResponse(byte result) {
    NettyMessage message = new NettyMessage();
    Header header = new Header();
    header.setType(MessageType.LOGIN_RESP.value());
    message.setHeader(header);
    message.setBody(result);
    return message;
  }
}
