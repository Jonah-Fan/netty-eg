package net.thewesthill.protocol.netty.codec;

import java.io.IOException;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufOutputStream;
import org.jboss.marshalling.Marshaller;
import org.jboss.marshalling.OutputStreamByteOutput;

public class MarshallingEncoder {

  private static final byte[] LENGTH_PLACEHOLDER = new byte[4];
  private final Marshaller marshaller;

  public MarshallingEncoder() throws IOException {
    marshaller = MarshallingCodecFactory.buildMarshalling();
  }

  protected void encode(Object msg, ByteBuf out) throws Exception {
    try {
      final int lengthPos = out.writerIndex();
      out.writeBytes(LENGTH_PLACEHOLDER);
      marshaller.start(new OutputStreamByteOutput(new ByteBufOutputStream(out)));
      marshaller.writeObject(msg);
      marshaller.finish();
      out.setInt(lengthPos, out.writerIndex() - lengthPos - 4);
    } finally {
      marshaller.close();
    }
  }
}
