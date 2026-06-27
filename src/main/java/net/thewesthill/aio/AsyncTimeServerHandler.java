package net.thewesthill.aio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.util.concurrent.CountDownLatch;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AsyncTimeServerHandler implements Runnable {

  private CountDownLatch latch;
  private AsynchronousServerSocketChannel asynchronousServerSocketChannel;

  public AsyncTimeServerHandler(int port) {
    try {
      asynchronousServerSocketChannel = AsynchronousServerSocketChannel.open();
      asynchronousServerSocketChannel.bind(new InetSocketAddress(port));
      log.info("The time server is start in port: {}", port);
    } catch (IOException e) {
      log.error("failed to start time server on port {}", port, e);
    }
  }

  @Override
  public void run() {
    latch = new CountDownLatch(1);
    doAccept();

    try {
      latch.await();
    } catch (InterruptedException e) {
      log.error("interrupted while awaiting latch", e);
    }
  }

  private void doAccept() {
    asynchronousServerSocketChannel.accept(this, new AcceptCompletionHandler());
  }

  AsynchronousServerSocketChannel getChannel() {
    return asynchronousServerSocketChannel;
  }

  CountDownLatch getLatch() {
    return latch;
  }
}
