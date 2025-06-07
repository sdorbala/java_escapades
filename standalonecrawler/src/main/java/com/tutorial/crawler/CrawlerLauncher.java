package com.tutorial.crawler;

import com.tutorial.crawler.service.ConcurrentCrawler;
import com.tutorial.crawler.util.ThreadsafeRateLimiter;

import org.pmw.tinylog.Logger;

public class CrawlerLauncher 
{
    public static void main( String[] args )
    {
        Logger.info("Hello, World! I am a ConcurrentCrawler. I am here to introduce you the concurrency programming in Java language." );
        // URL of the target webpage
        String seedUrl = "https://en.wikipedia.org/wiki/Main_Page";
        int concurrency = 10;

        Logger.info(String.format("Starting the crawler with seed URL: %s and concurrency level: %d", seedUrl, concurrency));
        ThreadsafeRateLimiter rateLimiter = new ThreadsafeRateLimiter(20, 1000);
        var crawler = new ConcurrentCrawler(seedUrl, concurrency, rateLimiter);
        crawler.beginCrawling();

        Logger.info( "Goodbye, World!. I hope you learned something today." );
    }
}
