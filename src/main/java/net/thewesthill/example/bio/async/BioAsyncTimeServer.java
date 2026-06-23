package net.thewesthill.example.bio.async;

import lombok.extern.slf4j.Slf4j;
import net.thewesthill.example.bio.sync.BioSyncTimeServer;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Slf4j
public class BioAsyncTimeServer {

    public static void main(String[] args) throws IOException {

        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(8080);
            log.info("The time server is start in port : " + 8080);
            Socket socket;
            TimeServerHandlerExecutePool singleExecutor = new TimeServerHandlerExecutePool(50, 10000);
            while (true) {
                socket = serverSocket.accept();
                singleExecutor.execute(new BioSyncTimeServer.TimeServerHandle(socket));
            }
        } finally {
            if (serverSocket != null) {
                log.info("The time server close");
                serverSocket.close();
            }
        }
    }

    public static class TimeServerHandlerExecutePool {

        private final ExecutorService executor;

        public TimeServerHandlerExecutePool(int maxPoolSize, int queueSize) {
            executor = new ThreadPoolExecutor(Runtime.getRuntime().availableProcessors(), maxPoolSize, 120L, TimeUnit.SECONDS, new ArrayBlockingQueue<>(queueSize));
        }

        public void execute(Runnable task) {
            executor.execute(task);
        }
    }
}
