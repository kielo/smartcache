package org.kielo.smartcache.aggregator

import org.kielo.smartcache.CountingAction
import org.kielo.smartcache.action.ActionResolvingException
import spock.lang.Specification

import java.util.concurrent.Executors

class RequestAggregatorTest extends Specification {

    private RequestAggregator aggregator = new RequestAggregator(Executors.newCachedThreadPool())

    def "should evaluate request and remove it from aggregator when completed"() {
        given:
        RequestQueueFuture<Integer> future = aggregator.aggregate("key", CountingAction.immediate())

        when:
        future.resolve(100)

        then:
        !aggregator.contains('key')
    }

    def "should evaluate request and remove it from aggregator when exception was thrown"() {
        given:
        RequestQueueFuture<Integer> future = aggregator.aggregate("key", CountingAction.failImmediately())

        when:
        future.resolve(100)

        then:
        thrown(ActionResolvingException)
        !aggregator.contains('key')
    }
}
