// ServerRunnable.java
package com.mobius.software.samples.oio.multiplethreads.runnableImpl;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ServerRunnable implements Runnable {

    private volatile boolean running = true;
    private ServerSocket serverSocket;
    private final List<String> messages = new ArrayList<>();

    public static void main(String[] args) {
        ServerRunnable server = new ServerRunnable();
        server.start();
    }

    @Override
    public void run() {
        start();
    }

    public void start() {
        ExecutorService executorService = Executors.newCachedThreadPool(); // Using a cached thread pool

        try {
            serverSocket = new ServerSocket(1230);
            System.out.println("Server started. Waiting for clients...");

            while (running) {
                try {
                    Socket socket = serverSocket.accept();
                    System.out.println("New client connected: " + socket);

                    // Start a new thread to handle the client
                    executorService.execute(new ClientHandler(socket, messages));
                } catch (IOException e) {
                    if (running) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            executorService.shutdown(); // Shut down the thread pool
        }
    }

    public void stop() {
        running = false;
        if (serverSocket != null && !serverSocket.isClosed()) {
            try {
                serverSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public List<String> getMessages() {
        synchronized (messages) {
            return new ArrayList<>(messages);
        }
    }
}



