package org.kielo.smartcache.metrics;

public interface CacheMetrics {

    void cacheHit(String region, String key, MetricsMetadata metricsMetadata);

    void staleCacheHit(String region, String key, MetricsMetadata metricsMetadata);

    void actionExecuted(String region, String key, MetricsMetadata metricsMetadata);

    Object actionResolutionStarted(String region, String key, MetricsMetadata metricsMetadata);

    void actionResolutionFinished(String region, String key, MetricsMetadata metricsMetadata, Object context);

    void actionTimeout(String region, String key, MetricsMetadata metricsMetadata);

    void actionError(String region, String key, MetricsMetadata metricsMetadata);
}

