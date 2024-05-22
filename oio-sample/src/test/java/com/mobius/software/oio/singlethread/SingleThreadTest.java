package com.mobius.software.oio.singlethread;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.mobius.software.samples.oio.singlethreaded.Client;
import com.mobius.software.samples.oio.singlethreaded.Server;

public class SingleThreadTest {

    private static final InputStream originalIn = System.in;
    private static final PrintStream originalOut = System.out;

    private static ByteArrayOutputStream mockOut;
    private static ByteArrayInputStream mockIn;

    @BeforeAll
    public static void setUpStreams() {
        mockOut = new ByteArrayOutputStream();
        System.setOut(new PrintStream(mockOut));

        // Prepare mock input for client
        String input = "Hello\nHow are you?\nBYE\n";
        mockIn = new ByteArrayInputStream(input.getBytes());
        System.setIn(mockIn);
    }

    @AfterAll
    public static void restoreStreams() {
        System.setIn(originalIn);
        System.setOut(originalOut);
    }

    @Test
    public void testClientServerCommunication() throws InterruptedException {
        // Start server in a separate thread
        CountDownLatch serverLatch = new CountDownLatch(1);
        Thread serverThread = new Thread(() -> {
            try {
                Server.main(new String[]{});
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                serverLatch.countDown();
            }
        });
        serverThread.start();

        // Give some time for the server to start
        TimeUnit.SECONDS.sleep(2);

        // Start client
        Thread clientThread = new Thread(() -> {
            try {
                Client.main(new String[]{});
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        clientThread.start();

        // Wait for server to finish
        serverLatch.await();

        // Check the output
        String serverOutput = mockOut.toString().trim();
        String expectedOutput = "THE CLIENT HAS CONNECTED\n" +
                "Client: Hello\n" +
                "Server: MESSAGE RECEIVED\n" +
                "Client: How are you?\n" +
                "Server: MESSAGE RECEIVED\n" +
                "Client: BYE\n" +
                "Server: MESSAGE RECEIVED\n" +
                "CLIENT HAS DISCONNECTED";

        assertEquals(expectedOutput, serverOutput);
    }
}


