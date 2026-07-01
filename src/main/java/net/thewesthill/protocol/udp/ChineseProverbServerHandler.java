package net.thewesthill.protocol.udp;

import java.util.concurrent.ThreadLocalRandom;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.DatagramPacket;
import io.netty.util.CharsetUtil;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ChineseProverbServerHandler extends SimpleChannelInboundHandler<DatagramPacket> {

  private static final String[] DICTIONARY = {
    "How could I bow and scrape to the powerful and noble, And let them take away the smile from my"
        + " face?",
    "rink while we may, for today we have wine; Tomorrow's sorrows will be tomorrow's concern.",
    "In life's prime moments, seize all joy you can; Let not your golden goblet mock the moon"
        + " unshared."
  };

  private String nextQuote() {
    int quoteId = ThreadLocalRandom.current().nextInt(DICTIONARY.length);
    return DICTIONARY[quoteId];
  }

  @Override
  protected void channelRead0(ChannelHandlerContext ctx, DatagramPacket msg) throws Exception {
    String req = msg.content().toString(CharsetUtil.UTF_8);
    log.info("{}", req);
    if (!"Proverb dictionary query?".equals(req)) {
      return;
    }
    ctx.writeAndFlush(
        new DatagramPacket(
            Unpooled.copiedBuffer("Proverb query result: " + nextQuote(), CharsetUtil.UTF_8),
            msg.sender()));
  }

  @Override
  public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
    log.info("channel exception", cause);
    ctx.close();
  }
}
