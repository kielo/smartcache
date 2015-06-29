package org.kielo.smartcache.spring

import com.codahale.metrics.MetricRegistry
import org.springframework.context.annotation.AnnotationConfigApplicationContext
import spock.lang.Specification

class SmartCacheAspectSpec extends Specification {

    def someBean;

    def metricRegistry

    def setup() {
        def context = new AnnotationConfigApplicationContext(SmartCacheAspectTestConfig)

        metricRegistry = context.getBean(MetricRegistry)
        someBean = context.getBean(SomeBean)
        someBean.reset()
    }

    def "should cache result"() {
        expect:
        someBean.counterMethodWithStandardCache() == 1
        someBean.counterMethodWithStandardCache() == 1
        getMetricCount("someBean.counterMethodWithStandardCache.service-request") == 1
        getMetricCount("someBean.counterMethodWithStandardCache.cache-hit") == 1
    }

    def "should call method when result expired"() {
        expect:
        someBean.counterMethodWithZeroCache() == 1
        Thread.sleep(1)
        someBean.counterMethodWithZeroCache() == 2
        getMetricCount("someBean.counterMethodWithZeroCache.service-request") == 2
        getMetricCount("someBean.counterMethodWithZeroCache.cache-hit") == 0
    }

    def "should return stale result when method throw exception but expired result is in cache"() {
        expect:
        someBean.secondCallExceptionMethodWithZeroCache() == 1
        Thread.sleep(1)
        someBean.secondCallExceptionMethodWithZeroCache() == 1
        getMetricCount("someBean.secondCallExceptionMethodWithZeroCache.service-request") == 1
        getMetricCount("someBean.secondCallExceptionMethodWithZeroCache.stale-cache-hit") == 1
    }

    def "should not cache failed result"() {
        when:
        someBean.firstCallExceptionMethodWithZeroCache()

        then:
        thrown(SomeBean.ServiceRuntimeException)

        when:
        Thread.sleep(10)
        def res = someBean.firstCallExceptionMethodWithZeroCache()

        then:
        res == 2
        getMetricCount("someBean.secondCallExceptionMethodWithZeroCache.service-request") == 1
        getMetricCount("someBean.secondCallExceptionMethodWithZeroCache.cache-hit") == 0
        getMetricCount("someBean.secondCallExceptionMethodWithZeroCache.error") == 1
    }

    private int getMetricCount(String name) {
        def meter = metricRegistry.getMetrics().get(name)

        if (meter) {
            return meter.count
        }

        0
    }
}
