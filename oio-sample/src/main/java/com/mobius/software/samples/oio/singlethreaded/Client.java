package com.mobius.software.samples.oio.singlethreaded;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class Client {
    public static void main(String[] args) throws IOException {
        Socket socket = null;

        try {
            // Connect to the server
            socket = new Socket("localhost", 1236);

            try (
                    // Use try-with-resources for automatic closing
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
                    Scanner scanner = new Scanner(System.in)
            ) {
                while (true) {
                    // Read message from user
                    String msgToSend = scanner.nextLine();
                    bufferedWriter.write(msgToSend + "\n"); // Add newline for server to read
                    bufferedWriter.flush();

                    // Receive response from server
                    String serverResponse = bufferedReader.readLine();
                    System.out.println("Server: " + serverResponse);

                    if (msgToSend.equalsIgnoreCase("BYE")) {
                        break;
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (socket != null) {
                socket.close();
            }
        }
    }
}