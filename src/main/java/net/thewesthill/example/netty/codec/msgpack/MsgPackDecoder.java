package net.thewesthill.example.netty.codec.msgpack;

import java.nio.ByteBuffer;
import java.util.List;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;

public class MsgPackDecoder extends MessageToMessageDecoder<ByteBuffer> {

  @Override
  protected void decode(
      ChannelHandlerContext ctx, ByteBuffer msg, List<Object> out) throws Exception {

  }

}
