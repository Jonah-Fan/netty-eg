package net.thewesthill.example.netty.codec.msgpack;

import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import org.msgpack.jackson.dataformat.MessagePackFactory;

import net.thewesthill.example.netty.codec.UserInfo;

public class MsgPackDecoder extends MessageToMessageDecoder<ByteBuf> {

  private final ObjectMapper objectMapper = new ObjectMapper(new MessagePackFactory());

  @Override
  protected void decode(
      ChannelHandlerContext ctx, ByteBuf msg, List<Object> out) throws Exception {
    byte[] array = new byte[msg.readableBytes()];
    msg.readBytes(array);

    UserInfo userInfo = objectMapper.readValue(array, UserInfo.class);
    out.add(userInfo);
  }

}
