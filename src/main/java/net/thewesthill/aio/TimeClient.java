package net.thewesthill.aio;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TimeClient {

  public static void main(String[] args) {
    int port = 8080;
    if (args != null && args.length > 0) {
      try {
        port = Integer.parseInt(args[0]);
      } catch (NumberFormatException e) {
        log.info(e.getMessage());
      }
    }
    new Thread(new AsyncTimeClientHandler("127.0.0.1", port), "AIO-AsyncTimeClientHandler-001")
        .start();
  }
}
