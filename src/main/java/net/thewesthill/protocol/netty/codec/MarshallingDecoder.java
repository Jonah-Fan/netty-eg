package net.thewesthill.protocol.netty.codec;

import java.io.IOException;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import org.jboss.marshalling.InputStreamByteInput;
import org.jboss.marshalling.Unmarshaller;

public class MarshallingDecoder {

  private Unmarshaller unmarshaller;

  public MarshallingDecoder() throws IOException {
    unmarshaller = MarshallingCodecFactory.buildUnmarshalling();
  }

  protected Object decode(ByteBuf in) throws Exception {
    try {
      int objectSize = in.readInt();
      ByteBuf buf = in.slice(in.readerIndex(), objectSize);
      unmarshaller.start(new InputStreamByteInput(new ByteBufInputStream(buf)));
      Object obj = unmarshaller.readObject();
      unmarshaller.finish();
      in.readerIndex(in.readerIndex() + objectSize);
      return obj;
    } finally {
      unmarshaller.close();
    }
  }
}
