package com.tutorial.crawler.worker;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.pmw.tinylog.Logger;

import com.tutorial.crawler.model.CrawlerResponseModel;
import com.tutorial.crawler.util.Constants;
import com.tutorial.crawler.util.IRateLimiter;
import com.tutorial.crawler.util.UrlHelper;

import lombok.Builder;

@Builder
public class PageCrawler implements Callable<CrawlerResponseModel> {
    private ExecutorService executorService;
    private String uri;
    private int depth;
    private ConcurrentSkipListSet<String> crawledSites;
    private volatile IRateLimiter rateLimiter;

    public CrawlerResponseModel call() throws Exception {
        try {
            Logger.info(Thread.currentThread().getName());
            Logger.info("Waiting to acquire a token to proceed...");
            rateLimiter.acquire();
            List<Future<CrawlerResponseModel>> childrenFutures = new ArrayList<>();
            if (UrlHelper.isUrlValid(this.uri) && !crawledSites.contains(this.uri)) {
                Logger.info("Crawling page: " + this.uri + " at depth: " + this.depth);
                crawledSites.add(uri);
                URI pageUri = UrlHelper.getUri(this.uri);
                
                // Document doc = Jsoup.parse(pageUri.toURL(), 10000);
                Document doc = Jsoup.connect(pageUri.toString()).ignoreHttpErrors(true).get();
                Elements linkTags = doc.select("a[href]");
                int newDepth = this.depth + 1;
                if (newDepth <= Constants.MAX_DEPTH) {
                    for (Element e : linkTags) {
                        String link = e.attr("href");
                        if (UrlHelper.isUrlValid(link) && !link.equals(this.uri)) {
                            if (link.startsWith("http") || link.startsWith("www")) {
                                URI linkUri = UrlHelper.getUri(link);
                                if (!linkUri.getHost().equalsIgnoreCase(pageUri.getHost())) {
                                    Logger.warn("Uri " + link + " linked to current page " + this.uri + " is not from the same site. Skipping this.");
                                } else {
                                    PageCrawler childCrawler = PageCrawler.builder()
                                        .uri(link)
                                        .crawledSites(crawledSites)
                                        .depth(newDepth)
                                        .executorService(executorService)
                                        .rateLimiter(rateLimiter)
                                        .build();
                                    childrenFutures.add(executorService.submit(childCrawler));
                                }
                            } else if (link.startsWith("/")){
                                link = this.uri + link.substring(1, link.length());
                                PageCrawler childCrawler = PageCrawler.builder()
                                        .uri(link)
                                        .crawledSites(crawledSites)
                                        .depth(newDepth)
                                        .executorService(executorService)
                                        .rateLimiter(rateLimiter)
                                        .build();
                                    childrenFutures.add(executorService.submit(childCrawler));
                            } else {
                                Logger.warn("Uri " + link + " linked to current page " + this.uri + " cannot be processed. Skipping this.");
                            }
                        } else {
                            Logger.warn("Uri " + link + " linked to current page " + this.uri + " cannot be processed. Skipping this.");
                        }
                    }
                }
            }
            return CrawlerResponseModel.builder()
                .childrenFutures(childrenFutures)
                .currentUrl(uri)
                .currentDepth(depth)
                .build();
        } catch (Exception ex) {
            throw ex; 
        }
    }
}
