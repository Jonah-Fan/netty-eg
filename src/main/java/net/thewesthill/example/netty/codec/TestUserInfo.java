package net.thewesthill.example.netty.codec;

import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

@Slf4j
public class TestUserInfo {

    public static void main(String[] args) throws IOException {
        UserInfo info = new UserInfo().buildUserId(100).buildUSerName("Welcome to Netty");
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream os = new ObjectOutputStream(bos);
        os.writeObject(info);
        os.flush();
        os.close();
        byte[] b = bos.toByteArray();
        log.info("The jdk serializable length is : {}", b.length);
        bos.close();
        log.info("----------------------------------------------");
        log.info("The byte array serializable length is : {}", info.codeC().length);
    }
}
