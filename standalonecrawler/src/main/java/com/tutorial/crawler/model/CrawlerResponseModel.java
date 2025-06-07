package com.tutorial.crawler.model;

import java.util.List;
import java.util.concurrent.Future;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class CrawlerResponseModel {
    private String currentUrl;
    private List<Future<CrawlerResponseModel>> childrenFutures;
    private int currentDepth;
    private String error;
}
