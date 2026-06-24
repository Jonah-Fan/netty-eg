package net.thewesthill.example.netty.codec;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.nio.ByteBuffer;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PerformTestUserInfo {

  public static void main(String[] args) throws IOException {
    UserInfo info = new UserInfo().buildUserName("Welcome to Netty");
    int loop = 1000000;
    ByteArrayOutputStream bos = null;
    ObjectOutputStream os = null;
    long startTime = System.currentTimeMillis();
    for (int i = 0; i < loop; i++) {
      bos = new ByteArrayOutputStream();
      os = new ObjectOutputStream(bos);
      os.writeObject(info);
      os.flush();
      os.close();
      bos.toByteArray();
      bos.close();
    }
    long endTime = System.currentTimeMillis();
    log.info("The jdk serializable cost time is : {} ms", endTime - startTime);
    log.info("------------------------------------------------------------------");
    ByteBuffer buffer = ByteBuffer.allocate(1024);
    startTime = System.currentTimeMillis();
    for (int i = 0; i < loop; i++) {
      info.codeC(buffer);
    }
    endTime = System.currentTimeMillis();
    log.info("The byte array serializable cost time is : {} ms", endTime - startTime);
  }
}