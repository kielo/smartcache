package org.kielo.smartcache

import com.jayway.awaitility.Awaitility
import org.kielo.smartcache.cache.Region
import org.kielo.smartcache.metrics.SimpleCacheMetrics
import spock.lang.Specification

import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class MetricsTest extends Specification {

    private SimpleCacheMetrics metrics = new SimpleCacheMetrics()

    private SmartCache cache = new SmartCache(Executors.newCachedThreadPool(), metrics)

    def setup() {
        cache.registerRegion(new Region('region', new EternalExpirationPolicy(), 5, 1000))
        cache.registerRegion(new Region('impatientRegion', new EternalExpirationPolicy(), 5, 20))
    }

    def "should trigger metric action when cache was queried"() {
        given:
        CountingAction action = CountingAction.failImmediately()

        when:
        cache.get('region', 'key', action)

        then:
        metrics.actionExecuted == 1
        metrics.cacheHits == 0
    }
    
    def "should trigger metric action when cache was queried and hit was found"() {
        given:
        CountingAction action = CountingAction.failImmediately()
        cache.put('region', 'key', -1)

        when:
        cache.get('region', 'key', action)

        then:
        metrics.actionExecuted == 0
        metrics.cacheHits == 1
        
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
    }
    
    def "should trigger metric action on exception"() {
        given:
        CountingAction action = CountingAction.failImmediately()

        when:
        cache.get('region', 'key', action)
        
        then:
        metrics.errors == 1
    }
}
