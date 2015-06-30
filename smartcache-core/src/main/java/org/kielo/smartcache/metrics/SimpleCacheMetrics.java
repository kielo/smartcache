package org.kielo.smartcache.metrics;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class SimpleCacheMetrics implements CacheMetrics {

    private AtomicInteger actionExecuted = new AtomicInteger();

    private AtomicInteger cacheHits = new AtomicInteger();

    private AtomicInteger staleCacheHit = new AtomicInteger();

    private AtomicLong totalResolutionTime = new AtomicLong();

    private AtomicInteger timeouts = new AtomicInteger();

    private AtomicInteger errors = new AtomicInteger();

    public int getActionExecuted() {
        return actionExecuted.get();
    }

    @Override
    public void cacheHit(String region, String key, MetricsMetadata metricsMetadata) {
        cacheHits.incrementAndGet();
    }

    @Override
    public void staleCacheHit(String region, String key, MetricsMetadata metricsMetadata) {
        staleCacheHit.incrementAndGet();
    }

    @Override
    public void actionExecuted(String region, String key, MetricsMetadata metricsMetadata) {
        actionExecuted.incrementAndGet();
    }

    public int getCacheHits() {
        return cacheHits.get();
    }

    @Override
    public Object actionResolutionStarted(String region, String key, MetricsMetadata metricsMetadata) {
        return System.currentTimeMillis();
    }


    @Override
    public void actionResolutionFinished(String region, String key, MetricsMetadata metricsMetadata, Object context) {
        totalResolutionTime.addAndGet((long) context);
    }

    public long getTotalResolutionTime() {
        return totalResolutionTime.get();
    }

    @Override
    public void actionTimeout(String region, String key, MetricsMetadata metricsMetadata) {
        timeouts.incrementAndGet();
    }

    public int getTimeouts() {
        return timeouts.get();
    }

    @Override
    public void actionError(String region, String key, MetricsMetadata metricsMetadata) {
        errors.incrementAndGet();
    }

    public int getErrors() {
        return errors.get();
    }
}
