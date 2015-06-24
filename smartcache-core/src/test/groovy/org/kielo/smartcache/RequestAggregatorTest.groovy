package org.kielo.smartcache

import spock.lang.Specification

import java.util.concurrent.Executors

class RequestAggregatorTest extends Specification {

    def "should evaluate request and remove it from aggregator when completed"() {
        given:
        RequestAggregator aggregator = new RequestAggregator(Executors.newCachedThreadPool())
        RequestQueueFuture<Integer> future = aggregator.aggregate("key", CountingAction.immediate())

        when:
        future.resolve(100)

        then:
        !aggregator.contains('key')
    }

    def "should evaluate request and remove it from aggregator when exception was thrown"() {
        given:
        RequestAggregator aggregator = new RequestAggregator(Executors.newCachedThreadPool())
        RequestQueueFuture<Integer> future = aggregator.aggregate("key", CountingAction.failImmediately())

        when:
        future.resolve(100)

        then:
        thrown()
        !aggregator.contains('key')
    }
}
