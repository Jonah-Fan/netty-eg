package net.thewesthill.bio;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TimeServer {

  public static void main(String[] args) throws IOException {
    ServerSocket serverSocket = null;
    try {
      serverSocket = new ServerSocket(8080);
      log.info("The time server is start in port : {}", 8080);
      Socket socket;
      while (true) {
        socket = serverSocket.accept();
        new Thread(new TimeServerHandle(socket)).start();
      }
    } finally {
      if (serverSocket != null) {
        log.info("The time server close");
        serverSocket.close();
      }
    }
  }

  public static class TimeServerHandle implements Runnable {

    private final Socket socket;

    public TimeServerHandle(Socket socket) {
      this.socket = socket;
    }

    @Override
    public void run() {
      BufferedReader in = null;
      PrintWriter out = null;
      try {
        in = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
        out = new PrintWriter(this.socket.getOutputStream(), true);
        String currentTime;
        String body;
        while (true) {
          body = in.readLine();
          if (body == null) {
            break;
          }
          log.info("The time server receive order : {}", body);
          currentTime =
              "QUERY TIME ORDER".equalsIgnoreCase(body)
                  ? new Date(System.currentTimeMillis()).toString()
                  : body;
          out.println(currentTime);
        }
      } catch (IOException e) {
        if (in != null) {
          try {
            in.close();
          } catch (IOException ex) {
            log.error("close failed", ex);
          }
        }

        if (out != null) {
          out.close();
        }

        try {
          this.socket.close();
        } catch (IOException ex) {
          log.error("close failed", ex);
        }
      }
    }
  }
}
