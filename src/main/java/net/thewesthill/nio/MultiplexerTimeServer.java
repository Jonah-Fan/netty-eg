package net.thewesthill.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Iterator;
import java.util.Set;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MultiplexerTimeServer implements Runnable {

  private Selector selector;

  private ServerSocketChannel servChannel;

  private volatile boolean stop;

  public MultiplexerTimeServer(int port) {
    try {
      selector = Selector.open();
      servChannel = ServerSocketChannel.open();
      servChannel.configureBlocking(false);
      servChannel.socket().bind(new InetSocketAddress(port), 1024);
      servChannel.register(selector, SelectionKey.OP_ACCEPT);
      log.info("The time server is start in port {}", port);
    } catch (IOException e) {
      log.info(e.getMessage());
      System.exit(1);
    }
  }

  public void stop() {
    this.stop = true;
  }

  @Override
  public void run() {
    while (!stop) {
      try {
        selector.select(1000);
        Set<SelectionKey> selectionKeys = selector.selectedKeys();
        Iterator<SelectionKey> it = selectionKeys.iterator();
        SelectionKey key;
        while (it.hasNext()) {
          key = it.next();
          it.remove();
          try {
            handleInput(key);
          } catch (Exception ex) {
            if (key != null) {
              key.cancel();
              if (key.channel() != null) {
                key.channel().close();
              }
            }
          }
        }
      } catch (IOException e) {
        log.info(e.getMessage());
      }
    }
  }

  private void handleInput(SelectionKey key) throws IOException {
    if (!key.isValid()) {
      return;
    }

    if (key.isAcceptable()) {
      ServerSocketChannel ssc = (ServerSocketChannel) key.channel();
      SocketChannel sc = ssc.accept();
      sc.configureBlocking(false);
      sc.register(selector, SelectionKey.OP_READ);
    } else if (key.isReadable()) {
      SocketChannel sc = (SocketChannel) key.channel();
      ByteBuffer readBuffer = ByteBuffer.allocate(1024);
      int readBytes = sc.read(readBuffer);
      if (readBytes < 0) {
        key.cancel();
        sc.close();
      } else if (readBytes > 0) {
        readBuffer.flip();
        byte[] bytes = new byte[readBuffer.remaining()];
        readBuffer.get(bytes);
        String body = new String(bytes, StandardCharsets.UTF_8);
        log.info("The time server receive order : {}", body);
        String currentTime =
            "QUERY TIME ORDER".equalsIgnoreCase(body)
                ? new Date(System.currentTimeMillis()).toString()
                : "BAD ORDER";
        doWrite(sc, currentTime);
      }
    }
  }

  private void doWrite(SocketChannel channel, String response) throws IOException {
    if (response != null && !response.trim().isEmpty()) {
      byte[] bytes = response.getBytes();
      ByteBuffer writeBuffer = ByteBuffer.allocate(bytes.length);
      writeBuffer.put(bytes);
      writeBuffer.flip();
      channel.write(writeBuffer);
    }
  }
}
