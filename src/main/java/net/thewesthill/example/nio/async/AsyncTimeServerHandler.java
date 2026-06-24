package net.thewesthill.example.nio.async;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.util.concurrent.CountDownLatch;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AsyncTimeServerHandler implements Runnable {

  CountDownLatch latch;
  AsynchronousServerSocketChannel asynchronousServerSocketChannel;

  public AsyncTimeServerHandler(int port) {
    try {
      asynchronousServerSocketChannel = AsynchronousServerSocketChannel.open();
      asynchronousServerSocketChannel.bind(new InetSocketAddress(port));
      log.info("The time server is start in port: {}", port);
    } catch (IOException e) {
      log.info(e.getMessage());
    }
  }

  @Override
  public void run() {
    latch = new CountDownLatch(1);
    doAccept();

    try {
      latch.await();
    } catch (InterruptedException e) {
      log.info(e.getMessage());
    }
  }

  public void doAccept() {
    asynchronousServerSocketChannel.accept(this, new AcceptCompletionHandler());
  }
}
