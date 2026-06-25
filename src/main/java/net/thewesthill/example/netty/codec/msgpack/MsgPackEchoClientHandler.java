package net.thewesthill.example.netty.codec.msgpack;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;

import net.thewesthill.example.netty.codec.UserInfo;

@Slf4j
public class MsgPackEchoClientHandler extends ChannelInboundHandlerAdapter {

  private final int sendNumber;

  public MsgPackEchoClientHandler(int sendNumber) {
    this.sendNumber = sendNumber;
  }

  @Override
  public void channelActive(ChannelHandlerContext ctx) throws Exception {
    UserInfo[] info = userInfo();
    for (UserInfo infoE : info) {
      ctx.write(infoE);
    }
    ctx.flush();
  }

  @Override
  public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
    UserInfo info = (UserInfo) msg;
    log.info("Client receive the msgpack message : {}", info.getUserName());
  }

  @Override
  public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
    ctx.flush();
  }

  @Override
  public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
    ctx.close();
  }

  private UserInfo[] userInfo() {
    UserInfo[] infos = new UserInfo[sendNumber];
    UserInfo userInfo = null;
    for (int i = 0; i < sendNumber; i++) {
      userInfo = new UserInfo().buildUserName("ABCDEFG ---> " + i);
      infos[i] = userInfo;
    }
    return infos;
  }
}
