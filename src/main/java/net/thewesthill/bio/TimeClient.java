package net.thewesthill.bio;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TimeClient {

  public static void main(String[] args) {

    Socket socket = null;
    BufferedReader in = null;
    PrintWriter out = null;
    try {
      socket = new Socket("127.0.0.1", 8080);
      in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
      out = new PrintWriter(socket.getOutputStream(), true);
      out.println("QUERY TIME ORDER");
      log.info("Send Order 2 server succeed.");
      String req = in.readLine();
      log.info("Now is : {}", req);
    } catch (IOException e) {
      log.error("time client failed", e);
    } finally {
      if (in != null) {
        try {
          in.close();
        } catch (IOException e) {
          log.error("failed to close reader", e);
        }
      }

      if (out != null) {
        out.close();
      }

      if (socket != null) {
        try {
          socket.close();
        } catch (IOException e) {
          log.error("failed to close socket", e);
        }
      }
    }
  }
}
