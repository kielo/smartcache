package org.kielo.smartcache.spring

import org.kielo.smartcache.spring.test.CachedTestBean
import org.kielo.smartcache.spring.test.TestApplication
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ContextConfiguration
import spock.lang.Specification

@ContextConfiguration(classes = TestApplication)
class SmartCacheAspectTest extends Specification {

    @Autowired
    CachedTestBean someBean

    def setup() {
        someBean.reset()
    }

    def "should cache result"() {
        expect:
        someBean.simpleInvocationCounter() == 1
        someBean.simpleInvocationCounter() == 1
    }

    def "should call method when result expired"() {
        expect:
        someBean.invocationCounterWithImmediateExpiration() == 1
        Thread.sleep(1)
        someBean.invocationCounterWithImmediateExpiration() == 2
    }

    def "should return stale result when method throw exception but expired result is in cache"() {
        given:
        someBean.throwsExceptionOnSecondInvocation()
        Thread.sleep(1)

        expect:
        someBean.throwsExceptionOnSecondInvocation() == 1
    }

    def "should not cache failed result"() {
        when:
        someBean.throwsExceptionOnFirstInvocation()

        then:
        thrown(CachedTestBean.ServiceRuntimeException)

        when:
        Thread.sleep(10)
        def result = someBean.throwsExceptionOnFirstInvocation()

        then:
        result == 2
    }
}
