package com.mobius.software.samples.oio.multiplethreads.runnableImpl;

import java.io.*;
import java.net.Socket;

public class ClientRunnable {

    public static void main(String[] args) {
        try {
            ClientRunnable client = new ClientRunnable();
            client.startClient(new String[]{"Hello", "How are you?", "BYE"});
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void startClient(String[] messages) throws IOException {
        try (Socket socket = new Socket("localhost", 1230)) {
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            for (String message : messages) {
                writer.write(message + "\n");
                writer.flush();
                System.out.println("Sent: " + message);
                String response = reader.readLine();
                System.out.println("Received: " + response);
            }
        }
    }
}
