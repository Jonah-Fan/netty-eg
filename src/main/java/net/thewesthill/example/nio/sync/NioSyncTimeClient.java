package net.thewesthill.example.nio.sync;

public class NioSyncTimeClient {

    public static void main(String[] args) {
        new Thread(new NioSyncTimeClientHandle("127.0.0.1", 8080), "TimeClient-001").start();
    }
}
