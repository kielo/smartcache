package org.kielo.smartcache.metrics;

public class NoopCacheMetrics implements CacheMetrics {

    @Override
    public void cacheHit(String region, String key, MetricsMetadata metricsMetadata) {
        // noop
    }

    @Override
    public void staleCacheHit(String region, String key, MetricsMetadata metricsMetadata) {
        // noop
    }

    @Override
    public void actionExecuted(String region, String key, MetricsMetadata metricsMetadata) {
        // noop
    }

    @Override
    public Object actionResolutionStarted(String region, String key, MetricsMetadata metricsMetadata) {
        return null;
    }

    @Override
    public void actionResolutionFinished(String region, String key, MetricsMetadata metricsMetadata, Object context) {
        // noop
    }

    @Override
    public void actionTimeout(String region, String key, MetricsMetadata metricsMetadata) {
        // noop
    }

    @Override
    public void actionError(String region, String key, MetricsMetadata metricsMetadata) {
        // noop
    }
}
