package org.kielo.smartcache.metrics;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;

import static org.kielo.smartcache.metrics.MeteredEventType.*;

public class MetricsSmartCacheMetrics implements SmartCacheMetrics {

    private final MetricRegistry metricRegistry;

    private final MetricNameSupplier nameSupplier;

    public MetricsSmartCacheMetrics(MetricRegistry metricRegistry, MetricNameSupplier nameSupplier1) {
        this.metricRegistry = metricRegistry;
        this.nameSupplier = nameSupplier1;
    }

    @Override
    public void cacheHit(String region, String key) {
        metricRegistry.meter(nameSupplier.nameFor(CACHE_HIT, region, key)).mark();
    }

    @Override
    public void staleCacheHit(String region, String key) {
        metricRegistry.meter(nameSupplier.nameFor(STALE_CACHE_HIT, region, key)).mark();
    }

    @Override
    public void actionExecuted(String region, String key) {
        metricRegistry.meter(nameSupplier.nameFor(ACTION_EXECUTED, region, key)).mark();
    }

    @Override
    public Object actionResolutionStarted(String region, String key) {
        return metricRegistry.timer(nameSupplier.nameFor(ACTION_TIMER, region, key)).time();
    }

    @Override
    public void actionResolutionFinished(String region, String key, Object context) {
        ((Timer.Context) context).stop();
    }

    @Override
    public void actionTimeout(String region, String key) {
        metricRegistry.meter(nameSupplier.nameFor(ACTION_TIMEOUT, region, key)).mark();
    }

    @Override
    public void actionError(String region, String key) {
        metricRegistry.meter(nameSupplier.nameFor(ACTION_ERROR, region, key)).mark();
    }
}
