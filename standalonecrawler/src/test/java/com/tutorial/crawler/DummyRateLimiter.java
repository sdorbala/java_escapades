package com.tutorial.crawler;

import com.tutorial.crawler.util.IRateLimiter;

public class DummyRateLimiter implements IRateLimiter {
    public void acquire() throws InterruptedException {

    }
    public void shutdown() {
        
    }
}
