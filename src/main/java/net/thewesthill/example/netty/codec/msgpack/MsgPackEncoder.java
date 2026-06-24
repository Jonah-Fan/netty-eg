package net.thewesthill.example.netty.codec.msgpack;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import org.msgpack.MessagePack;

public class MsgPackEncoder extends MessageToByteEncoder<Object> {

  @Override
  protected void encode(ChannelHandlerContext arg0, Object arg1, ByteBuf arg2) throws Exception {
    MessagePack msgPack = new MessagePack();
    // Serialize arg[0].msg
    byte[] raw = msgPack.write(arg1);
    arg2.writeBytes(raw);
  }
}
