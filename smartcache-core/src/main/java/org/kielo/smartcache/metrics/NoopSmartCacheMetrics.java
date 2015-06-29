package org.kielo.smartcache.metrics;

public class NoopSmartCacheMetrics implements SmartCacheMetrics {

    @Override
    public void cacheHit(String region, String key) {
        // noop
    }

    @Override
    public void staleCacheHit(String region, String key) {
        // noop
    }

    @Override
    public void actionExecuted(String region, String key) {
        // noop
    }

    @Override
    public Object actionResolutionStarted(String region, String key) {
        return null;
    }

    @Override
    public void actionResolutionFinished(String region, String key, Object context) {
        // noop
    }

    @Override
    public void actionTimeout(String region, String key) {
        // noop
    }

    @Override
    public void actionError(String region, String key) {
        // noop
    }
}
