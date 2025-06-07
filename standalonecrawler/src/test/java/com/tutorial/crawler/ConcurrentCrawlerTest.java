package com.tutorial.crawler;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.tutorial.crawler.service.ConcurrentCrawler;

public class ConcurrentCrawlerTest {
    @Test
    public void ConcurrentCrawlerConstructorTests() {
        ConcurrentCrawler crawler = new ConcurrentCrawler("www.example.com", 10, new DummyRateLimiter());
        assertEquals("www.example.com", crawler.getSeedUrl());
        assertEquals(10, crawler.getConcurrency());
    }
}
