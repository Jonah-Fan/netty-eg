package net.thewesthill.protocol.http;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelProgressiveFuture;
import io.netty.channel.ChannelProgressiveFutureListener;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.DefaultHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpUtil;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.LastHttpContent;
import io.netty.handler.stream.ChunkedFile;
import jakarta.activation.MimetypesFileTypeMap;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class HttpFileServerHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

  private final String url;

  public HttpFileServerHandler(String url) {
    this.url = url;
  }

  @Override
  protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest msg) throws Exception {
    if (!msg.decoderResult().isSuccess()) {
      sendError(ctx, HttpResponseStatus.BAD_REQUEST);
      return;
    }

    if (!msg.method().equals(HttpMethod.GET)) {
      sendError(ctx, HttpResponseStatus.METHOD_NOT_ALLOWED);
      return;
    }

    final String uri = msg.uri();
    final String path = sanitizeUri(uri);
    if (path == null) {
      sendError(ctx, HttpResponseStatus.FORBIDDEN);
      return;
    }

    File file = new File(path);
    if (file.isDirectory()) {
      if (uri.endsWith("/")) {
        sendListing(ctx, file);
      } else {
        sendRedirect(ctx, uri + '/');
      }
      return;
    }

    if (!file.isFile()) {
      sendError(ctx, HttpResponseStatus.FORBIDDEN);
      return;
    }
    RandomAccessFile randomAccessFile = null;
    try {
      randomAccessFile = new RandomAccessFile(file, "r");
    } catch (FileNotFoundException e) {
      sendError(ctx, HttpResponseStatus.NOT_FOUND);
      return;
    }

    long fileLength = randomAccessFile.length();
    HttpResponse response = new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
    HttpUtil.setContentLength(response, fileLength);
    setContentTypeHeader(response, file);
    if (HttpUtil.isKeepAlive(msg)) {
      response.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
    }
    ctx.write(response);
    ChannelFuture sendFileFuture = null;
    sendFileFuture =
        ctx.write(
            new ChunkedFile(randomAccessFile, 0, fileLength, 8192), ctx.newProgressivePromise());
    sendFileFuture.addListener(
        new ChannelProgressiveFutureListener() {
          @Override
          public void operationProgressed(
              ChannelProgressiveFuture future, long progress, long total) throws Exception {
            if (total < 0) {
              log.info("Transfer progress: {}", progress);
            } else {
              log.info("Transfer progress: {} / {}", progress, total);
            }
          }

          @Override
          public void operationComplete(ChannelProgressiveFuture future) throws Exception {
            log.info("Transfer compete.");
          }
        });
    ChannelFuture lasChannelFuture = ctx.writeAndFlush(LastHttpContent.EMPTY_LAST_CONTENT);
    if (!HttpUtil.isKeepAlive(msg)) {
      lasChannelFuture.addListener(ChannelFutureListener.CLOSE);
    }
  }

  @Override
  public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
    cause.printStackTrace();
    if (ctx.channel().isActive()) {
      sendError(ctx, HttpResponseStatus.INTERNAL_SERVER_ERROR);
    }
  }

  private static final Pattern INSECURE_URI = Pattern.compile(".*[<>&\"].*");

  private String sanitizeUri(String uri) {
    try {
      uri = URLDecoder.decode(uri, StandardCharsets.UTF_8.name());
    } catch (UnsupportedEncodingException e1) {
      try {
        uri = URLDecoder.decode(uri, StandardCharsets.ISO_8859_1.name());
      } catch (UnsupportedEncodingException e2) {
        throw new Error(e1);
      }
    }

    if (!uri.startsWith(url)) {
      return null;
    }

    if (!uri.startsWith("/")) {
      return null;
    }

    uri = uri.replace('/', File.separatorChar);
    if (uri.contains(File.separator + '.')
        || uri.contains("." + File.separator)
        || uri.startsWith(".")
        || uri.endsWith(".")
        || INSECURE_URI.matcher(uri).matches()) {
      return null;
    }
    return System.getProperty("user.dir") + File.separator + uri;
  }

  private static final Pattern ALLOWED_FILE_NAME = Pattern.compile("[A-Za-z0-9][-_A-Za-z0-9\\.]*");

  private static void sendListing(ChannelHandlerContext ctx, File dir) {
    FullHttpResponse response =
        new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
    response.headers().set(HttpHeaderNames.CONTENT_TYPE, HttpHeaderValues.TEXT_HTML);
    StringBuilder buf = new StringBuilder();
    String dirPath = dir.getParent();
    buf.append("<!DOCTYPE html>\r\n");
    buf.append("<html><head><title>");
    buf.append(dirPath);
    buf.append("directory: ");
    buf.append("</title></head><body>\r\n");
    buf.append("<h3>");
    buf.append(dirPath).append(" directory: ");
    buf.append("</h3>\r\n");
    buf.append("<ul>");
    buf.append("<li>link: <a href=\"../\">..</a></li>\r\n");
    for (File f : dir.listFiles()) {
      if (f.isHidden() || !f.canRead()) {
        continue;
      }
      String name = f.getName();
      if (!ALLOWED_FILE_NAME.matcher(name).matches()) {
        continue;
      }
      buf.append("<li>link: <a href=\"");
      buf.append(name);
      buf.append("\">");
      buf.append(name);
      buf.append("</a></li>\r\n");
    }
    buf.append("</ul></body></html>\r\n");
    ByteBuf buffer = Unpooled.copiedBuffer(buf, StandardCharsets.UTF_8);
    response.content().writeBytes(buffer);
    buffer.release();
    ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
  }

  private static void sendRedirect(ChannelHandlerContext ctx, String newUri) {
    FullHttpResponse response =
        new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.FOUND);
    response.headers().set(HttpHeaderNames.LOCATION, newUri);
    ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
  }

  private static void sendError(ChannelHandlerContext ctx, HttpResponseStatus status) {
    FullHttpResponse response =
        new DefaultFullHttpResponse(
            HttpVersion.HTTP_1_1,
            status,
            Unpooled.copiedBuffer(
                "Failure: " + status.toString() + "\r\n", StandardCharsets.UTF_8));
    response.headers().set(HttpHeaderNames.CONTENT_TYPE, HttpHeaderValues.TEXT_PLAIN);
    ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
  }

  private static void setContentTypeHeader(HttpResponse resp, File file) {
    MimetypesFileTypeMap mimeFileTypeMap = new MimetypesFileTypeMap();
    resp.headers()
        .set(HttpHeaderNames.CONTENT_TYPE, mimeFileTypeMap.getContentType(file.getPath()));
  }
}
