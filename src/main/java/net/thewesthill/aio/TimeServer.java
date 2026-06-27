package net.thewesthill.aio;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TimeServer {

  public static void main(String[] args) {
    int port = 8080;
    if (args != null && args.length > 0) {
      try {
        port = Integer.parseInt(args[0]);
      } catch (NumberFormatException e) {
        log.error("invalid port argument: {}", args[0], e);
      }
    }
    AsyncTimeServerHandler timeServer = new AsyncTimeServerHandler(port);
    new Thread(timeServer, "AIO-AsyncTimeServerHandler-001").start();
  }
}
