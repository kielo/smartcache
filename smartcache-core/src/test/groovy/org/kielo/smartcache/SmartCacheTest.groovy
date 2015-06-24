package org.kielo.smartcache

import org.kielo.smartcache.action.ActionResult
import org.kielo.smartcache.cache.Region
import spock.lang.Specification

import java.util.concurrent.Executors

class SmartCacheTest extends Specification {

    private SmartCache cache = new SmartCache(Executors.newCachedThreadPool())

    def setup() {
        cache.registerRegion(new Region('region', new EternalExpirationPolicy(), 5, 1000))      
        cache.registerRegion(new Region('immediateRegion', new ImmediateExpirationPolicy(), 5, 1000))      
        cache.registerRegion(new Region('impatientRegion', new EternalExpirationPolicy(), 5, 20))
    }
    
    def "should return value from successful action even if there is other cached"() {
        given:
        CountingAction action = CountingAction.immediate()
        cache.put('region', 'key', -10)

        when:
        ActionResult result = cache.get('region', 'key', action)

        then:
        result.result() == 1
    }

    def "should return cached value on action error"() {
        given:
        CountingAction action = CountingAction.failImmediately()
        cache.put('region', 'key', 100)
        
        when:
        ActionResult result = cache.get('region', 'key', action)

        then:
        result.result() == 100
        result.fromCache
        result.failed()
        result.caughtException() instanceof IllegalStateException
    }

    def "should return cached value on action timeout"() {
        given:
        CountingAction action = CountingAction.waiting(50)
        cache.put('impatientRegion', 'key', 100)

        when:
        ActionResult result = cache.get('impatientRegion', 'key', action)

        then:
        result.result() == 100
        result.fromCache
        result.timeout()
    }
    
    def "should not run two request for same key at the same time"() {
        given:
        CountingAction action = CountingAction.waiting(50)
        
        when:
        cache.get('region', 'key', action)
        cache.get('region', 'key', action)
        
        then:
        action.counter == 1
    }
    
    def "should not refresh cache when fresh key is already there"() {
        given:
        CountingAction action = CountingAction.immediate()
        
        when:
        cache.get('region', 'key', action)
        cache.get('region', 'key', action)
        
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

    def "should indicate if action was cache hit"() {
        given:
        CountingAction action = CountingAction.immediate()
        
        when:
        ActionResult first = cache.get('region', 'key', action)
        ActionResult second = cache.get('region', 'key', action)
        
        then:
        !first.isFromCache()
        second.isFromCache()
    }

}
