package com.mobius.software.samples.oio.multiplethreads.runnableImpl;

import java.io.*;
import java.net.Socket;
import java.util.List;

public class ClientHandler implements Runnable {
    private final Socket socket;
    private final List<String> messages;

    public ClientHandler(Socket socket, List<String> messages) {
        this.socket = socket;
        this.messages = messages;
    }

    @Override
    public void run() {
        try (
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()))) {
            String msgFromClient;
            while ((msgFromClient = bufferedReader.readLine()) != null) {
                System.out.println("Client " + socket + ": " + msgFromClient);
                synchronized (messages) {
                    messages.add(msgFromClient);
                }

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

