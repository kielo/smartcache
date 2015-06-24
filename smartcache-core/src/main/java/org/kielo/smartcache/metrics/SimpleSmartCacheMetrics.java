package org.kielo.smartcache.metrics;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class SimpleSmartCacheMetrics implements SmartCacheMetrics {

    private AtomicInteger cacheHits = new AtomicInteger();

    private AtomicInteger cacheMisses = new AtomicInteger();

    private AtomicLong totalResolutionTime = new AtomicLong();

    private AtomicInteger timeouts = new AtomicInteger();

    private AtomicInteger errors = new AtomicInteger();

    @Override
    public void cacheHit(String region, String key) {
        cacheHits.incrementAndGet();
    }

    public int getCacheHits() {
        return cacheHits.get();
    }

    @Override
    public void cacheMiss(String region, String key) {
        cacheMisses.incrementAndGet();
    }

    public int getCacheMisses() {
        return cacheMisses.get();
    }

    @Override
    public Object actionResolutionStarted(String region, String key) {
        return System.currentTimeMillis();
    }


    @Override
    public void actionResolutionFinished(String region, String key, Object context) {
        totalResolutionTime.addAndGet((long) context);
    }

    public long getTotalResolutionTime() {
        return totalResolutionTime.get();
    }

    @Override
    public void actionTimeout(String region, String key) {
        timeouts.incrementAndGet();
    }

    public int getTimeouts() {
        return timeouts.get();
    }

    @Override
    public void actionError(String region, String key) {
        errors.incrementAndGet();
    }

    public int getErrors() {
        return errors.get();
    }
}
