package net.thewesthill.example.nio.sync;

public class NioSyncTimeServer {

    public static void main(String[] args) {
        NioSyncMultiplexerTimeServer timeServer = new NioSyncMultiplexerTimeServer(8080);
        new Thread(timeServer, "NIO-MultiplexerTimeServer-001").start();
    }
}
