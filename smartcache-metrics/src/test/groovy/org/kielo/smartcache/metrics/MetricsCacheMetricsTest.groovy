package org.kielo.smartcache.metrics

import com.codahale.metrics.MetricRegistry
import spock.lang.Specification

class MetricsCacheMetricsTest extends Specification {
    
    private MetricRegistry registry = new MetricRegistry()
    
    private MetricNameSupplier nameSupplier = { event, region, key,  metadata -> (String) "$event-$region-$key" }
    
    private MetricsCacheMetrics metrics = new MetricsCacheMetrics(registry, nameSupplier)
    
    private MetricsMetadata metadata = new MetricsMetadata('')
    
    def "should measure cache hits"() {
        when:
        metrics.cacheHit('region', 'key', metadata)
        
        then:
        registry.getMeters()['CACHE_HIT-region-key'].count == 1
    }

    def "should measure stale cache hits"() {
        when:
        metrics.staleCacheHit('region', 'key', metadata)

        then:
        registry.getMeters()['STALE_CACHE_HIT-region-key'].count == 1
    }

    def "should measure action executions"() {
        when:
        metrics.actionExecuted('region', 'key', metadata)

        then:
        registry.getMeters()['ACTION_EXECUTED-region-key'].count == 1
    }

    def "should measure action errors"() {
        when:
        metrics.actionError('region', 'key', metadata)

        then:
        registry.getMeters()['ACTION_ERROR-region-key'].count == 1
    }

    def "should measure action timeouts"() {
        when:
        metrics.actionTimeout('region', 'key', metadata)

        then:
        registry.getMeters()['ACTION_TIMEOUT-region-key'].count == 1
    }

    def "should measure action execution time"() {
        when:
        Object context = metrics.actionResolutionStarted('region', 'key', metadata)
        metrics.actionResolutionFinished('region', 'key', metadata, context)

        then:
        registry.getTimers()['ACTION_TIMER-region-key'].count == 1
    }
}
