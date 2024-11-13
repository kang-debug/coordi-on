package com.fivefull.coordiback.util;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ImageCache {

    private final Map<String, CacheItem> cache = new ConcurrentHashMap<>();
    private final long expirationTime;

    public ImageCache(long expirationTimeMillis) {
        this.expirationTime = expirationTimeMillis;
    }

    public List<String> get(String keyword) {
        CacheItem item = cache.get(keyword);
        if (item != null && System.currentTimeMillis() - item.timestamp < expirationTime) {
            return item.urls;
        }
        return null;
    }

    public void put(String keyword, List<String> urls) {
        cache.put(keyword, new CacheItem(urls));
    }

    private static class CacheItem {
        private final List<String> urls;
        private final long timestamp;

        public CacheItem(List<String> urls) {
            this.urls = urls;
            this.timestamp = System.currentTimeMillis();
        }
    }
}
