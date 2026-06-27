package net.thewesthill.protocol.file;

import java.io.File;
import java.io.RandomAccessFile;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.DefaultFileRegion;
import io.netty.channel.FileRegion;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class FileServerHandler extends SimpleChannelInboundHandler<String> {

  @Override
  protected void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {
    File file = new File(msg);
    if (!file.exists()) {
      ctx.writeAndFlush("File not found: " + file + System.lineSeparator());
      return;
    }
    if (!file.isFile()) {
      ctx.writeAndFlush("Not a file : " + file + System.lineSeparator());
      return;
    }

    ctx.write(file + " " + file.length() + System.lineSeparator());

    try (RandomAccessFile raf = new RandomAccessFile(file, "r")) {
      FileRegion region = new DefaultFileRegion(raf.getChannel(), 0, raf.length());
      ctx.write(region);
      ctx.writeAndFlush(System.lineSeparator());
    }
  }

  @Override
  public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
    log.error("channel exception", cause);
    ctx.close();
  }
}
