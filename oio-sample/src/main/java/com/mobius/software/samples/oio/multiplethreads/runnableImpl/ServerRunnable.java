package com.mobius.software.samples.oio.multiplethreads.runnableImpl;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ServerRunnable {

    public static void main(String[] args) {
        ServerRunnable server = new ServerRunnable();
        server.startServer();
    }

    public void startServer() {
        ExecutorService executorService = Executors.newCachedThreadPool(); // Using a cached thread pool

        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(1230);
            System.out.println("Server started. Waiting for clients...");

            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("New client connected: " + socket);

                // Start a new thread to handle the client
                Thread clientThread = new Thread(new ClientHandler(socket));
                clientThread.start(); // Start the client handler thread
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (serverSocket != null) {
                try {
                    serverSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            executorService.shutdown(); // Shut down the thread pool
        }
    }

    private static class ClientHandler implements Runnable {
        private final Socket socket;

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try (
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()))) {
                String msgFromClient;
                while ((msgFromClient = bufferedReader.readLine()) != null) {
                    System.out.println("Client " + socket + ": " + msgFromClient);

                    // Send server response immediately after receiving client message
                    bufferedWriter.write("Server: MESSAGE RECEIVED\n");
                    bufferedWriter.flush();

                    if (msgFromClient.equalsIgnoreCase("BYE")) {
                        break;
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    socket.close();
                    System.out.println("Client disconnected: " + socket);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}

