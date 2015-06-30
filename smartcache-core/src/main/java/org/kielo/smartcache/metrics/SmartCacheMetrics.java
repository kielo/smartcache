package org.kielo.smartcache.metrics;

public interface SmartCacheMetrics {

    void cacheHit(String region, String key);

    void staleCacheHit(String region, String key);

    void actionExecuted(String region, String key);

    Object actionResolutionStarted(String region, String key);

    void actionResolutionFinished(String region, String key, Object context);

    void actionTimeout(String region, String key);

    void actionError(String region, String key);
}

