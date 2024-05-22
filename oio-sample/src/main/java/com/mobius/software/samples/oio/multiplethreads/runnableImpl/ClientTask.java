// ClientTask.java
package com.mobius.software.samples.oio.multiplethreads.runnableImpl;

import java.util.concurrent.CountDownLatch;

public class ClientTask implements Runnable {
    private final CountDownLatch latch;
    private final String[] messages;

    public ClientTask(CountDownLatch latch, String[] messages) {
        this.latch = latch;
        this.messages = messages;
    }

    @Override
    public void run() {
        try {
            ClientRunnable client = new ClientRunnable(messages);
            client.start();
        } finally {
            latch.countDown();
        }
    }
}
