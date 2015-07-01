package org.kielo.smartcache

import org.kielo.smartcache.action.ActionResult
import org.kielo.smartcache.cache.Region
import spock.lang.Specification

import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class SmartCacheTest extends Specification {

    private SmartCache cache = new SmartCache(Executors.newCachedThreadPool())

    def setup() {
        cache.registerRegion(new Region('region', new EternalExpirationPolicy(), 5))
        cache.registerRegion(new Region('immediateRegion', new ImmediateExpirationPolicy(), 5))
    }

    def "should return value from resolved action"() {
        given:
        CountingAction action = CountingAction.immediate()

        when:
        int value = cache.get('key', Integer).fromRegion('region').invoke(action).result()

        then:
        value == 1
    }
    
    def "should return value from cache if not expired"() {
        given:
        CountingAction action = CountingAction.immediate()
        cache.put('region', 'key', -10)

        when:
        ActionResult result =  cache.get('key').fromRegion('region').invoke(action)

        then:
        result.result() == -10
    }

    def "should rerun action when value in cache have expired"() {
        given:
        CountingAction action = CountingAction.immediate()

        when:
        cache.get('key').fromRegion('immediateRegion').invoke(action)
        cache.get('key').fromRegion('immediateRegion').invoke(action)

        then:
        action.counter == 2
    }

    def "should not cache failed results"() {
        given:
        CountingAction action = CountingAction.failingOn(0)

        when:
        ActionResult failedResult = cache.get('key').fromRegion('region').invoke(action)
        ActionResult result = cache.get('key').fromRegion('region').invoke(action)

        then:
        failedResult.caughtException() instanceof IllegalStateException
        result.result() == 2
    }

    def "should return stale cached value on action error"() {
        given:
        CountingAction action = CountingAction.failImmediately()
        cache.put('immediateRegion', 'key', 100)
        sleep(10)

        when:
        ActionResult result = cache.get('key').fromRegion('immediateRegion').invoke(action)

        then:
        result.result() == 100
        result.fromStaleCache
    }

    def "should return stale cached value on action timeout"() {
        given:
        CountingAction action = CountingAction.waiting(50)
        cache.put('immediateRegion', 'key', 100)
        sleep(10)

        when:
        ActionResult result = cache.get('key').fromRegion('immediateRegion').withTimeout(20).invoke(action)

        then:
        result.result() == 100
        result.fromStaleCache
        result.timeout()
    }

    def "should not run two request for same key at the same time"() {
        given:
        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountingAction action = CountingAction.waiting(100)

        when:
        executor.submit({ cache.get('key').fromRegion('region').invoke(action) })
        executor.submit({ cache.get('key').fromRegion('region').invoke(action) }).get()

        then:
        action.counter == 1
    }
    
    def "should return no value when first invocation of action failed"() {
        given:
        CountingAction action = CountingAction.failImmediately()

        when:
        ActionResult result = cache.get('key').fromRegion('region').invoke(action)

        then:
        !result.result()
        result.failedWithoutCacheHit()
    }
}
