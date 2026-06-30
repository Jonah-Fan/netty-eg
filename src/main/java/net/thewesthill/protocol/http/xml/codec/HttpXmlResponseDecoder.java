package net.thewesthill.protocol.http.xml.codec;

import java.util.List;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpResponse;

public class HttpXmlResponseDecoder extends AbstractHttpXmlDecoder<FullHttpResponse> {

  public HttpXmlResponseDecoder(Class<?> clazz) {
    this(clazz, false);
  }

  public HttpXmlResponseDecoder(Class<?> clazz, boolean isPrint) {
    super(clazz, isPrint);
  }

  @Override
  protected void decode(ChannelHandlerContext ctx, FullHttpResponse msg, List<Object> out)
      throws Exception {
    HttpXmlResponse response = new HttpXmlResponse(msg, decode0(ctx, msg.content()));
    out.add(response);
  }
}
