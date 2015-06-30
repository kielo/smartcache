package org.kielo.smartcache.metrics

import com.codahale.metrics.MetricRegistry
import spock.lang.Specification

class MetricsSmartCacheMetricsTest extends Specification {
    
    private MetricRegistry registry = new MetricRegistry()
    
    private MetricNameSupplier nameSupplier = { event, region, key -> (String) "$event-$region-$key" }
    
    private MetricsSmartCacheMetrics metrics = new MetricsSmartCacheMetrics(registry, nameSupplier)
    
    def "should measure cache hits"() {
        when:
        metrics.cacheHit('region', 'key')
        
        then:
        registry.getMeters()['CACHE_HIT-region-key'].count == 1
    }

    def "should measure stale cache hits"() {
        when:
        metrics.staleCacheHit('region', 'key')

        then:
        registry.getMeters()['STALE_CACHE_HIT-region-key'].count == 1
    }

    def "should measure action executions"() {
        when:
        metrics.actionExecuted('region', 'key')

        then:
        registry.getMeters()['ACTION_EXECUTED-region-key'].count == 1
    }

    def "should measure action errors"() {
        when:
        metrics.actionError('region', 'key')

        then:
        registry.getMeters()['ACTION_ERROR-region-key'].count == 1
    }

    def "should measure action timeouts"() {
        when:
        metrics.actionTimeout('region', 'key')

        then:
        registry.getMeters()['ACTION_TIMEOUT-region-key'].count == 1
    }

    def "should measure action execution time"() {
        when:
        Object context = metrics.actionResolutionStarted('region', 'key')
        metrics.actionResolutionFinished('region', 'key', context)

        then:
        registry.getTimers()['ACTION_TIMER-region-key'].count == 1
    }
}
