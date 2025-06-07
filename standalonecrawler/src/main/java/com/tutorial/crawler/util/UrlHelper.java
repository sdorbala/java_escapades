package com.tutorial.crawler.util;

import java.net.URI;

public final class UrlHelper {
    public static boolean isUrlValid(String url) {
        return !url.contains("javascript") && !url.contains("#");
    }

    public static URI getUri(String urlString) {
        return URI.create(urlString);
    }
}
