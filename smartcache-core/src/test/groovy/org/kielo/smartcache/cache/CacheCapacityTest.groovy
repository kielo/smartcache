package org.kielo.smartcache.cache

import org.kielo.smartcache.CountingAction
import org.kielo.smartcache.EternalExpirationPolicy
import org.kielo.smartcache.SmartCache
import spock.lang.Specification

import java.util.concurrent.Executors

class CacheCapacityTest extends Specification {

    private SmartCache cache = new SmartCache(Executors.newCachedThreadPool())
    
    def setup() {
    }
    
    def "should not exceed given capacity"() {
        given:
        int capacity = 1
        cache.registerRegion(new Region('bounded capacity region', new EternalExpirationPolicy(), capacity, 1000))
        CountingAction action = CountingAction.immediate();
        
        cache.put('region', 'key0', 'capacity')
        cache.put('region', 'key1', 'replacing oldest')
        
        when:
        cache.get('region', 'key0', action)
        
        then:
        // this means there was cache miss and action indeed was run
        action.counter == 1
    }
    
}
