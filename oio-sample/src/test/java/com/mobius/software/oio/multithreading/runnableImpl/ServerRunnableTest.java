package com.mobius.software.oio.multithreading.runnableImpl;

import static org.junit.Assert.assertTrue;

import java.io.*;
import java.util.concurrent.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.mobius.software.samples.oio.multiplethreads.runnableImpl.ClientRunnable;
import com.mobius.software.samples.oio.multiplethreads.runnableImpl.ServerRunnable;

public class ServerRunnableTest {
    private final ByteArrayOutputStream outputStreamCaptor = new ByteArrayOutputStream();
    private final PrintStream standardOut = System.out;

    @Before
    public void setUp() {
        //System.setOut(new PrintStream(outputStreamCaptor));
    }

    @After
    public void tearDown() {
        System.setOut(standardOut);
    }

    @Test
    public void testClientServerCommunication() throws IOException, InterruptedException {
        ExecutorService executorService = Executors.newFixedThreadPool(3); // Increase thread pool size to handle multiple clients
        CountDownLatch latch = new CountDownLatch(2); // Adjust count for the main thread and two client threads

        // Start server in a separate thread
        executorService.execute(new ServerTask());

        // Delay to ensure the server starts before the clients
        Thread.sleep(1000);

        // Start multiple clients
        for (int i = 0; i < 2; i++) {
            executorService.execute(new ClientTask(latch));
        }

        latch.await();

        // Shutdown executor service
        executorService.shutdown();
        executorService.awaitTermination(1, TimeUnit.SECONDS);

        // Print the communication dialogue to the console
        String consoleOutput = outputStreamCaptor.toString();
        System.out.println("Communication Dialogue:\n" + consoleOutput);

        // Verify the communication by checking the console output
        assertTrue("Console output should contain 'Hello'", consoleOutput.contains("Hello"));
        assertTrue("Console output should contain 'How are you?'", consoleOutput.contains("How are you?"));
        assertTrue("Console output should contain 'BYE'", consoleOutput.contains("BYE"));
        assertTrue("Console output should contain 'Server: MESSAGE RECEIVED'", consoleOutput.contains("Server: MESSAGE RECEIVED"));
    }
}

class ServerTask implements Runnable {

    public ServerTask() {
 
    }

    @Override
    public void run() {
        ServerRunnable server = new ServerRunnable();
        server.startServer();
    }
}

class ClientTask implements Runnable {
    private final CountDownLatch latch;

    public ClientTask(CountDownLatch latch) {
        this.latch = latch;
    }

    @Override
    public void run() {
        try {
            ClientRunnable client = new ClientRunnable();
            client.startClient(new String[]{"Hello", "How are you?", "BYE"});
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            latch.countDown();
        }
    }
}
