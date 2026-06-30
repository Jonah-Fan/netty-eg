package net.thewesthill.protocol.http.xml.codec;

import java.io.StringReader;
import java.nio.charset.Charset;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import lombok.extern.slf4j.Slf4j;
import org.jibx.runtime.BindingDirectory;
import org.jibx.runtime.IBindingFactory;
import org.jibx.runtime.IUnmarshallingContext;
import org.jibx.runtime.JiBXException;

@Slf4j
public abstract class AbstractHttpXmlDecoder<T> extends MessageToMessageDecoder<T> {

  private IBindingFactory factory = null;

  private StringReader reader = null;

  private Class<?> clazz;

  private boolean isPrint;

  private static final String CHARSET_NAME = "UTF-8";

  private static final Charset UTF_8 = Charset.forName(CHARSET_NAME);

  protected AbstractHttpXmlDecoder(Class<?> clazz) {
    this(clazz, false);
  }

  protected AbstractHttpXmlDecoder(Class<?> clazz, boolean isPrint) {
    this.clazz = clazz;
    this.isPrint = isPrint;
  }

  protected Object decode0(ChannelHandlerContext ctx, ByteBuf body) throws JiBXException {
    factory = BindingDirectory.getFactory(clazz);
    String content = body.toString(UTF_8);
    if (isPrint) {
      log.info("The body is : {}", content);
    }
    reader = new StringReader(content);
    IUnmarshallingContext uctx = factory.createUnmarshallingContext();
    Object res = uctx.unmarshalDocument(reader);
    return res;
  }
}
