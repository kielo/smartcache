package org.kielo.smartcache.metrics;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;

import static org.kielo.smartcache.metrics.MeteredEventType.*;

public class MetricsCacheMetrics implements CacheMetrics {

    private final MetricRegistry metricRegistry;

    private final MetricNameSupplier nameSupplier;

    public MetricsCacheMetrics(MetricRegistry metricRegistry, MetricNameSupplier nameSupplier) {
        this.metricRegistry = metricRegistry;
        this.nameSupplier = nameSupplier;
    }

    @Override
    public void cacheHit(String region, String key, MetricsMetadata metricsMetadata) {
        metricRegistry.meter(nameSupplier.nameFor(CACHE_HIT, region, key, metricsMetadata)).mark();
    }

    @Override
    public void staleCacheHit(String region, String key, MetricsMetadata metricsMetadata) {
        metricRegistry.meter(nameSupplier.nameFor(STALE_CACHE_HIT, region, key, metricsMetadata)).mark();
    }

    @Override
    public void actionExecuted(String region, String key, MetricsMetadata metricsMetadata) {
        metricRegistry.meter(nameSupplier.nameFor(ACTION_EXECUTED, region, key, metricsMetadata)).mark();
    }

    @Override
    public Object actionResolutionStarted(String region, String key, MetricsMetadata metricsMetadata) {
        return metricRegistry.timer(nameSupplier.nameFor(ACTION_TIMER, region, key, metricsMetadata)).time();
    }

    @Override
    public void actionResolutionFinished(String region, String key, MetricsMetadata metricsMetadata, Object context) {
        ((Timer.Context) context).stop();
    }

    @Override
    public void actionTimeout(String region, String key, MetricsMetadata metricsMetadata) {
        metricRegistry.meter(nameSupplier.nameFor(ACTION_TIMEOUT, region, key, metricsMetadata)).mark();
    }

    @Override
    public void actionError(String region, String key, MetricsMetadata metricsMetadata) {
        metricRegistry.meter(nameSupplier.nameFor(ACTION_ERROR, region, key, metricsMetadata)).mark();
    }
}
