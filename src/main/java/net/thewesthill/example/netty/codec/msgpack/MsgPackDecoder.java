package net.thewesthill.example.netty.codec.msgpack;

import java.util.List;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import org.msgpack.MessagePack;

public class MsgPackDecoder extends MessageToMessageDecoder<ByteBuf> {

  @Override
  protected void decode(
      ChannelHandlerContext ctx, ByteBuf msg, List<Object> out) throws Exception {
    final byte[] array;
    final int length = msg.readableBytes();
    array = new byte[length];
    msg.getBytes(msg.readerIndex(), array, 0, length);
    MessagePack msgPack = new MessagePack();
    out.add(msgPack.read(array));
  }

}
