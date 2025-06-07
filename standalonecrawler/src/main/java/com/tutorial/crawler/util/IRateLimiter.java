package com.tutorial.crawler.util;

public interface IRateLimiter {
    public void acquire() throws InterruptedException;
    public void shutdown();
}
