package net.thewesthill.protocol.netty.codec;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;

import net.thewesthill.protocol.netty.struct.Header;
import net.thewesthill.protocol.netty.struct.NettyMessage;

public class NettyMessageDecoder extends LengthFieldBasedFrameDecoder {

  private MarshallingDecoder marshallingDecoder;

  public NettyMessageDecoder(int maxFrameLength, int lengthFieldOffset, int lengthFieldLength)
      throws IOException {
    super(maxFrameLength, lengthFieldOffset, lengthFieldLength);
    marshallingDecoder = new MarshallingDecoder();
  }

  public NettyMessageDecoder(
      int maxFrameLength,
      int lengthFieldOffset,
      int lengthFieldLength,
      int lengthAdjustment,
      int initialBytesToStrip)
      throws IOException {
    super(
        maxFrameLength,
        lengthFieldOffset,
        lengthFieldLength,
        lengthAdjustment,
        initialBytesToStrip);
    marshallingDecoder = new MarshallingDecoder();
  }

  @Override
  protected Object decode(ChannelHandlerContext ctx, ByteBuf in) throws Exception {
    ByteBuf frame = (ByteBuf) super.decode(ctx, in);
    if (frame == null) {
      return null;
    }

    try {
      final NettyMessage message = new NettyMessage();
      Header header = new Header();
      header.setCrcCode(frame.readInt());
      header.setLength(frame.readInt());
      header.setSessionId(frame.readLong());
      header.setType(frame.readByte());
      header.setPriority(frame.readByte());

      int size = frame.readInt();
      if (size > 0) {
        Map<String, Object> attch = new HashMap<>(size);
        for (int i = 0; i < size; i++) {
          int keySize = frame.readInt();
          byte[] keyArray = new byte[keySize];
          frame.readBytes(keyArray);
          String key = new String(keyArray, StandardCharsets.UTF_8);
          attch.put(key, marshallingDecoder.decode(frame));
        }
        header.setAttachment(attch);
      }

      if (frame.readableBytes() > 4) {
        message.setBody(marshallingDecoder.decode(frame));
      }
      message.setHeader(header);
      return message;
    } finally {
      frame.release();
    }
  }
}
