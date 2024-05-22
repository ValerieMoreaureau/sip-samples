package com.mobius.software.oio.multithreading.runnableImpl;

import static org.junit.Assert.assertTrue;

import java.io.*;
import java.util.List;
import java.util.concurrent.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.mobius.software.samples.oio.multiplethreads.runnableImpl.ClientTask;
import com.mobius.software.samples.oio.multiplethreads.runnableImpl.ServerRunnable;

public class ServerRunnableTest {
    private final ByteArrayOutputStream outputStreamCaptor = new ByteArrayOutputStream();
    private final PrintStream standardOut = System.out;

    private ExecutorService executorService;
    private ServerRunnable serverRunnable;

    @Before
    public void setUp() {
        System.setOut(new PrintStream(outputStreamCaptor));
        executorService = Executors.newFixedThreadPool(3); // Increased thread pool size to handle multiple clients
        serverRunnable = new ServerRunnable();
        executorService.execute(serverRunnable);
    }

    @After
    public void tearDown() {
        serverRunnable.stop();
        executorService.shutdownNow();
        System.setOut(standardOut);
    }

    @Test
    public void testClientServerCommunication() throws IOException, InterruptedException {
        CountDownLatch latch = new CountDownLatch(2); // Adjust count for the main thread and two client threads

        // Delay to ensure the server starts before the clients
        Thread.sleep(1000);

        // Start multiple clients
        for (int i = 0; i < 2; i++) {
            executorService.execute(new ClientTask(latch, new String[]{"Hello", "How are you?", "BYE"}));
        }

        latch.await();

        // Shutdown executor service
        executorService.shutdown();
        executorService.awaitTermination(1, TimeUnit.SECONDS);

        // Print the communication dialogue to the console
        System.setOut(standardOut);
        String consoleOutput = outputStreamCaptor.toString();
        System.out.println("Communication Dialogue:\n" + consoleOutput);

        // Verify the communication by checking the console output
        assertTrue("Console output should contain 'Hello'", consoleOutput.contains("Hello"));
        assertTrue("Console output should contain 'How are you?'", consoleOutput.contains("How are you?"));
        assertTrue("Console output should contain 'BYE'", consoleOutput.contains("BYE"));
        assertTrue("Console output should contain 'Server: MESSAGE RECEIVED'", consoleOutput.contains("Server: MESSAGE RECEIVED"));

        // Verify messages saved on the server
        List<String> messages = serverRunnable.getMessages();
        assertTrue("Messages should contain 'Hello'", messages.contains("Hello"));
        assertTrue("Messages should contain 'How are you?'", messages.contains("How are you?"));
        assertTrue("Messages should contain 'BYE'", messages.contains("BYE"));
    }
}


