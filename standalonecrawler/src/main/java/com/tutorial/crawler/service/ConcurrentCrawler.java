package com.tutorial.crawler.service;

import java.util.*;
import java.util.concurrent.*;

import org.pmw.tinylog.Logger;

import com.tutorial.crawler.model.CrawlerResponseModel;
import com.tutorial.crawler.util.IRateLimiter;
import com.tutorial.crawler.worker.PageCrawler;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class ConcurrentCrawler {
    private String seedUrl;
    private int concurrency;
    private IRateLimiter rateLimiter;

    public boolean beginCrawling() {
        ExecutorService executorService = Executors.newFixedThreadPool(concurrency);
        Map<String, List> result = new HashMap<>();
        List<String> errors = new ArrayList<>();
        try {
            Logger.info("Starting the crawler with seed URL: " + seedUrl);
            PageCrawler seedCrawler = PageCrawler.builder()
                .uri(seedUrl)
                .crawledSites(new ConcurrentSkipListSet<>())
                .depth(1)
                .executorService(executorService)
                .rateLimiter(rateLimiter)
                .build();
            List<Map> seedResult = crawlRecursive(Collections.singletonList(executorService.submit(seedCrawler)), errors);
            result.put(seedUrl, seedResult);
        } catch (Exception ie) {
            Logger.error(ie, ie.getMessage());
            // only the top most one??
        } finally {
            rateLimiter.shutdown(); // stop refilling the tokenBucket
            executorService.shutdown();
            while (!executorService.isTerminated()) {
                Logger.info("Shutting down executorService...");
            }
            if(!errors.isEmpty()) {
                Logger.error(errors.toString());
                return false;
            }
        }
        return true;
    }

    private List<Map> crawlRecursive(List<Future<CrawlerResponseModel>> futures, List<String> errors) {
        Logger.info("Starting calls to linked pages recursively.");
        List<Map> parents = new ArrayList<>();
        for (Future<CrawlerResponseModel> future : futures) {
            Map<String, List<Map>> parentResponse = new HashMap<>();
            try {
                CrawlerResponseModel crawlerResponseModel = future.get(); // this blocks??
                List<Map> children = new ArrayList<>();
                Logger.info("Processed Url: " + crawlerResponseModel.getCurrentUrl());
                var childrenFutures = crawlerResponseModel.getChildrenFutures();
                if (!childrenFutures.isEmpty()) {
                    children.addAll(crawlRecursive(childrenFutures, errors));
                }
                parentResponse.put(crawlerResponseModel.getCurrentUrl(), children);
                parents.add(parentResponse);
            } catch (Exception e) {
                Logger.error(e, e.getMessage());
                errors.add(e.getMessage());
            }
        }
        Logger.info("Ending recursive calls.");
        return parents;
    }
}
