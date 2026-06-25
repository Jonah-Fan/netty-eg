package net.thewesthill.example.netty.codec.msgpack;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import net.thewesthill.example.netty.codec.UserInfo;
import org.msgpack.jackson.dataformat.MessagePackFactory;

import java.util.List;

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
