package org.kielo.smartcache.spring.test

import org.kielo.smartcache.spring.SmartCached;
import org.springframework.stereotype.Component;

@Component
public class CachedTestBean {

    private int standardCacheCounter = 0
    private int zeroCacheCounter = 0
    private int expectedSecondExceptionCounter = 0;
    private int expectedFirstExceptionCounter = 0;

    void reset() {
        standardCacheCounter = 0
        zeroCacheCounter = 0
        expectedSecondExceptionCounter = 0
        expectedFirstExceptionCounter = 0
    }

    @SmartCached(region='standardCache')
    int simpleInvocationCounter() {
        standardCacheCounter++
        return standardCacheCounter
    }

    @SmartCached(region='impatientCache')
    int invocationCounterWithImmediateExpiration() {
        zeroCacheCounter++
        return zeroCacheCounter
    }

    @SmartCached(region='impatientCache')
    int throwsExceptionOnSecondInvocation() {
        expectedSecondExceptionCounter++
        if (expectedSecondExceptionCounter == 2) {
            throw new RuntimeException()
        }
        
        return expectedSecondExceptionCounter
    }

    @SmartCached(region='impatientCache')
    int throwsExceptionOnFirstInvocation() {
        expectedFirstExceptionCounter++
        if (expectedFirstExceptionCounter == 1) {
            throw new ServiceRuntimeException(expectedFirstExceptionCounter)
        }

        return expectedFirstExceptionCounter
    }

    class ServiceRuntimeException extends RuntimeException {
        ServiceRuntimeException(int counter) {
            super("Counter state: $counter")
        }
    }

}
