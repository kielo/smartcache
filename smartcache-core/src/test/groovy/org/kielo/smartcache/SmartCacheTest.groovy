package org.kielo.smartcache

import org.kielo.smartcache.action.ActionResult
import org.kielo.smartcache.cache.Region
import spock.lang.Specification

import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.Future

class SmartCacheTest extends Specification {

    private SmartCache cache = new SmartCache(Executors.newCachedThreadPool())

    def setup() {
        cache.registerRegion(new Region('region', new EternalExpirationPolicy(), 5, 10000))
        cache.registerRegion(new Region('immediateRegion', new ImmediateExpirationPolicy(), 5, 1000))
        cache.registerRegion(new Region('impatientRegion', new ImmediateExpirationPolicy(), 5, 20))
    }
    
    def "should return value from cache if not expire"() {
        given:
        CountingAction action = CountingAction.immediate()
        cache.put('region', 'key', -10)

        when:
        ActionResult result = cache.get('region', 'key', action)

        then:
        result.result() == -10
    }

    def "should return stale cached value on action error"() {
        given:
        CountingAction action = CountingAction.failImmediately()
        cache.put('immediateRegion', 'key', 100)
        sleep(10)
        
        when:
        ActionResult result = cache.get('immediateRegion', 'key', action)

        then:
        result.result() == 100
        result.fromCache
        result.failed()
        result.caughtException() instanceof IllegalStateException
    }

    def "should return stale cached value on action timeout"() {
        given:
        CountingAction action = CountingAction.waiting(50)
        cache.put('impatientRegion', 'key', 100)
        sleep(10)

        when:
        ActionResult result = cache.get('impatientRegion', 'key', action)

        then:
        result.result() == 100
        result.fromCache
        result.timeout()
    }
    
    def "should not run two request for same key at the same time"() {
        given:
        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountingAction action = CountingAction.waiting(100)
        
        when:
        executor.submit({ cache.get('region', 'key', action) })
        executor.submit({ cache.get('region', 'key', action) }).get()

        then:
        action.counter == 1
    }
    
    def "should rerun action when value in cache have expired"() {
        given:
        CountingAction action = CountingAction.immediate()
        
        when:
        cache.get('immediateRegion', 'key', action)
        cache.get('immediateRegion', 'key', action)
        
        then:
        action.counter == 2
    }

    def "should return value from resolved action"() {
        given:
        CountingAction action = CountingAction.immediate()
        
        when:
        int value = cache.get('region', 'key', action).result()
        
        then:
        value == 1
    }
    
    def "should not cache failed results"() {
        given:
        CountingAction action = CountingAction.failingOn(0)
        
        when:
        ActionResult failedResult = cache.get('region', 'key', action)
        ActionResult result = cache.get('region', 'key', action)
        
        then:
        failedResult.caughtException() instanceof IllegalStateException
        result.result() == 2
    }
}
