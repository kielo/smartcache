package org.kielo.smartcache.metrics;

public interface SmartCacheMetrics {
    
    void cacheQueried(String region, String key);
    
    void cacheHit(String region, String key);
    
    Object actionResolutionStarted(String region, String key);
    
    void actionResolutionFinished(String region, String key, Object context);
    
    void actionTimeout(String region, String key);
    
    void actionError(String region, String key);
    
    
}
