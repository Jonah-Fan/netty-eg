package net.thewesthill.example.netty.codec.msgpack;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import org.msgpack.jackson.dataformat.MessagePackFactory;

import net.thewesthill.example.netty.codec.UserInfo;

public class MsgPackEncoder extends MessageToByteEncoder<UserInfo> {

  private final ObjectMapper objectMapper = new ObjectMapper(new MessagePackFactory());

  @Override
  protected void encode(ChannelHandlerContext arg0, UserInfo arg1, ByteBuf arg2) throws Exception {
    byte[] raw = objectMapper.writeValueAsBytes(arg1);
    arg2.writeBytes(raw);
  }
}
