package net.thewesthill.example.nio.async;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CountDownLatch;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AsyncTimeClientHandler implements CompletionHandler<Void, AsyncTimeClientHandler>,
    Runnable {

  private final String host;
  private final int port;
  private AsynchronousSocketChannel client;
  private CountDownLatch latch;

  public AsyncTimeClientHandler(String host, int port) {
    this.host = host;
    this.port = port;
    try {
      client = AsynchronousSocketChannel.open();
    } catch (IOException e) {
      log.info(e.getMessage());
    }
  }

  @Override
  public void run() {
    latch = new CountDownLatch(1);
    client.connect(new InetSocketAddress(host, port), this, this);
    try {
      latch.await();
    } catch (InterruptedException e) {
      log.info(e.getMessage());
    }
    try {
      client.close();
    } catch (IOException e) {
      log.info(e.getMessage());
    }
  }

  @Override
  public void completed(Void result, AsyncTimeClientHandler attachment) {
    byte[] req = "QUERY TIME ORDER".getBytes();
    ByteBuffer writeBuffer = ByteBuffer.allocate(req.length);
    writeBuffer.put(req);
    writeBuffer.flip();
    client.write(writeBuffer, writeBuffer, new CompletionHandler<>() {
      @Override
      public void completed(Integer result, ByteBuffer attachment) {
        if (attachment.hasRemaining()) {
          client.write(attachment, attachment, this);
        } else {
          ByteBuffer readBuffer = ByteBuffer.allocate(1024);
          client.read(readBuffer, readBuffer, new CompletionHandler<>() {
            @Override
            public void completed(Integer result, ByteBuffer attachment) {
              attachment.flip();
              byte[] bytes = new byte[attachment.remaining()];
              attachment.get(bytes);
              String body = new String(bytes, StandardCharsets.UTF_8);
              log.info("Now is {}", body);
              latch.countDown();
            }

            @Override
            public void failed(Throwable exc, ByteBuffer attachment) {
              try {
                client.close();
                latch.countDown();
              } catch (IOException e) {
                log.info(e.getMessage());
              }
            }
          });
        }
      }

      @Override
      public void failed(Throwable exc, ByteBuffer attachment) {
        try {
          client.close();
          latch.countDown();
        } catch (IOException e) {
          log.info(e.getMessage());
        }
      }
    });
  }

  @Override
  public void failed(Throwable exc, AsyncTimeClientHandler attachment) {
    log.info(exc.getMessage());
    try {
      client.close();
      latch.countDown();
    } catch (IOException e) {
      log.info(e.getMessage());
    }
  }
}
