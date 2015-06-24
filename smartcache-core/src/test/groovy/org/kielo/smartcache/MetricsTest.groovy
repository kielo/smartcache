package org.kielo.smartcache

import com.jayway.awaitility.Awaitility
import org.kielo.smartcache.cache.Region
import org.kielo.smartcache.metrics.SimpleSmartCacheMetrics
import spock.lang.Specification

import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class MetricsTest extends Specification {

    private SimpleSmartCacheMetrics metrics = new SimpleSmartCacheMetrics()

    private SmartCache cache = new SmartCache(Executors.newCachedThreadPool(), metrics)

    def setup() {
        cache.registerRegion(new Region('region', new EternalExpirationPolicy(), 5, 1000))
        cache.registerRegion(new Region('impatientRegion', new EternalExpirationPolicy(), 5, 20))
    }

    def "should trigger metric action on cache hit and cache miss"() {
        given:
        CountingAction action = CountingAction.immediate()

        when:
        cache.get('region', 'key', action)
        cache.get('region', 'key', action)

        then:
        metrics.cacheHits == 1
        metrics.cacheMisses == 1
    }

    def "should trigger metric action to measure execution time"() {
        given:
        CountingAction action = CountingAction.waiting(20)

        when:
        cache.get('region', 'key0', action)
        cache.get('region', 'key1', action)

        then:
        Awaitility.await().pollInterval(10, TimeUnit.MILLISECONDS).timeout(50, TimeUnit.MILLISECONDS)
        metrics.totalResolutionTime > 40
    }

    def "should trigger metric action on timeout"() {
        given:
        CountingAction action = CountingAction.waiting(100)

        when:
        cache.get('impatientRegion', 'key', action)

        then:
        metrics.timeouts == 1
        metrics.cacheMisses == 1
    }
    
    def "should trigger metric action on exception"() {
        given:
        CountingAction action = CountingAction.failImmediately()

        when:
        cache.get('region', 'key', action)
        
        then:
        metrics.errors == 1
        metrics.cacheMisses == 1
    }
}
