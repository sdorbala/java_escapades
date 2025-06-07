package com.tutorial.crawler.util;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class ThreadsafeRateLimiter implements IRateLimiter {
    private final ReentrantLock lock = new ReentrantLock();
    private final Condition availableTokens = lock.newCondition();
    private volatile BlockingQueue<Object> tokenBucket; // Represents available tokens

    private final int capacity;
    private final long refillRateMillis; // How often tokens are added
    private long lastRefillTime;
    private boolean exitRefillThread = false;

    public ThreadsafeRateLimiter(int capacity, long refillRateMillis) {
        this.capacity = capacity;
        this.refillRateMillis = refillRateMillis;
        this.tokenBucket = new ArrayBlockingQueue<>(capacity);
        this.lastRefillTime = System.currentTimeMillis();
        // Initially fill the bucket
        for (int i = 0; i < capacity; i++) {
            tokenBucket.offer(new Object()); // Add dummy tokens
        }
        Thread refillThread = new Thread(() -> {
            while (!exitRefillThread || !Thread.currentThread().isInterrupted()) {
                try {
                    Thread.sleep(refillRateMillis);
                    lock.lock();
                    refillTokens();
                } catch (InterruptedException interrupted) {
                    exitRefillThread = true;
                } finally {
                    lock.unlock();
                }
            }
        });
        refillThread.setDaemon(true);
        refillThread.start();
    }

    private void refillTokens() {
        long now = System.currentTimeMillis();
        long timeElapsed = now - lastRefillTime;
        if (timeElapsed >= refillRateMillis) {
            int tokensToAdd = (int) (timeElapsed / refillRateMillis);
            for (int i = 0; i < tokensToAdd && tokenBucket.size() < capacity; i++) {
                tokenBucket.offer(new Object());
            }
            lastRefillTime = now;
            availableTokens.signalAll(); // Signal waiting threads that tokens might be available
        }
    }

    public void acquire() throws InterruptedException {
        lock.lock();
        try {
            while (tokenBucket.isEmpty()) {
                availableTokens.await(); // Wait if no tokens are available
            }
            tokenBucket.take(); // Consume a token
        } finally {
            lock.unlock();
        }
    }

    // Should be some privileged operation
    public void shutdown() {
        exitRefillThread = true;
    }
}
