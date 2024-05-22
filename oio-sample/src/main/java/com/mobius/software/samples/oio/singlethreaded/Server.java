package com.mobius.software.samples.oio.singlethreaded;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {

    public static void main(String[] args) throws IOException {

        ServerSocket serverSocket = null;
        boolean clientDisconnected = false;

        try {
            serverSocket = new ServerSocket(1236);

            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("THE CLIENT HAS CONNECTED");

                try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                     BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()))) {

                    while (true) {
                        String msgFromClient = bufferedReader.readLine();
                        System.out.println("Client: " + msgFromClient);

                        bufferedWriter.write("MESSAGE RECEIVED\n");
                        bufferedWriter.flush();

                        if (msgFromClient.equalsIgnoreCase("BYE")) {
                            clientDisconnected = true; // Set flag when client disconnects
                            break;
                        }
                    }
                } finally {
                    socket.close();
                    System.out.println("CLIENT HAS DISCONNECTED");
                    if (clientDisconnected) {
                        break; // Terminate server if client has disconnected
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (serverSocket != null) {
                serverSocket.close();
            }
        }
    }
}