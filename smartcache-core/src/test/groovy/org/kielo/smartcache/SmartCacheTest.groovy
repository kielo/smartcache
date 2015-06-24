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
    }
    
    def "should put object into cache"() {
        when:
        cache.put('region', 'key', 'value')
        
        then:
        cache.get('region', 'key', null) == 'value'
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
