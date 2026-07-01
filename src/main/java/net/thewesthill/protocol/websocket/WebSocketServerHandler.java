package net.thewesthill.protocol.websocket;

import java.util.Date;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpUtil;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PingWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PongWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshakerFactory;
import io.netty.util.CharsetUtil;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class WebSocketServerHandler extends SimpleChannelInboundHandler<Object> {

  private WebSocketServerHandshaker handshaker;

  @Override
  protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
    if (msg instanceof FullHttpRequest) {
      handleHttpRequest(ctx, (FullHttpRequest) msg);
    } else if (msg instanceof WebSocketFrame) {
      handleWebSocketFrame(ctx, (WebSocketFrame) msg);
    }
  }

  @Override
  public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
    ctx.flush();
  }

  @Override
  public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
    log.error("channel exception", cause);
    ctx.close();
  }

  private void handleHttpRequest(ChannelHandlerContext ctx, FullHttpRequest request) {
    if (!request.decoderResult().isSuccess()
        || (!"websocket".equals(request.headers().get("Upgrade")))) {
      sendHttpResponse(
          ctx,
          request,
          new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.BAD_REQUEST));
      return;
    }

    WebSocketServerHandshakerFactory factory =
        new WebSocketServerHandshakerFactory("ws://localhost:8080/websocket", null, false);
    handshaker = factory.newHandshaker(request);
    if (handshaker == null) {
      WebSocketServerHandshakerFactory.sendUnsupportedVersionResponse(ctx.channel());
    } else {
      handshaker.handshake(ctx.channel(), request);
    }
  }

  private void handleWebSocketFrame(ChannelHandlerContext ctx, WebSocketFrame frame) {
    if (frame instanceof CloseWebSocketFrame) {
      handshaker.close(ctx.channel(), (CloseWebSocketFrame) frame.retain());
      return;
    }

    if (frame instanceof PingWebSocketFrame) {
      ctx.channel().write(new PongWebSocketFrame(frame.content().retain()));
      return;
    }

    if (!(frame instanceof TextWebSocketFrame)) {
      throw new UnsupportedOperationException(
          String.format("%s frame types not supported", frame.getClass().getName()));
    }

    String request = ((TextWebSocketFrame) frame).text();
    log.info("{} received {}", ctx.channel(), request);
    ctx.channel()
        .write(
            new TextWebSocketFrame(
                request + " , Welcome Netty Websocket Server, Now: " + new Date().toString()));
  }

  private void sendHttpResponse(
      ChannelHandlerContext ctx, FullHttpRequest request, FullHttpResponse response) {
    if (response.status().code() != 200) {
      ByteBuf buf = Unpooled.copiedBuffer(response.status().toString(), CharsetUtil.UTF_8);
      response.content().writeBytes(buf);
      buf.release();
      HttpUtil.setContentLength(response, response.content().readableBytes());
    }

    ChannelFuture f = ctx.channel().writeAndFlush(response);
    if (!HttpUtil.isKeepAlive(request) || response.status().code() != 200) {
      f.addListener(ChannelFutureListener.CLOSE);
    }
  }
}
