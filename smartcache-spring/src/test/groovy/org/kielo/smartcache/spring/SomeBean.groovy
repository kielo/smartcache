package org.kielo.smartcache.spring

class SomeBean {

    private int standardCacheCounter = 0
    private int zeroCacheCounter = 0
    private int expectedSecondExceptionCounter = 0;
    private int expectedFirstExceptionCounter = 0;

    def reset() {
        standardCacheCounter = 0
        zeroCacheCounter = 0
        expectedSecondExceptionCounter = 0
        expectedFirstExceptionCounter = 0
    }

    @SmartCached(region="standardCache", metricPrefix = "someBean.counterMethodWithStandardCache")
    def counterMethodWithStandardCache() {
        ++standardCacheCounter
    }

    @SmartCached(region="zeroCache", metricPrefix = "someBean.counterMethodWithZeroCache")
    def counterMethodWithZeroCache() {
        ++zeroCacheCounter
    }

    @SmartCached(region="zeroCache", metricPrefix = "someBean.secondCallExceptionMethodWithZeroCache")
    def secondCallExceptionMethodWithZeroCache() {
        if (expectedSecondExceptionCounter == 1) {
            throw new RuntimeException()
        }

        ++expectedSecondExceptionCounter
    }

    @SmartCached(region="zeroCache", metricPrefix = "someBean.firstCallExceptionMethodWithZeroCache")
    def firstCallExceptionMethodWithZeroCache() {
        println "firstCallExceptionMethodWithZeroCache() ..."
        ++expectedFirstExceptionCounter
        if (expectedFirstExceptionCounter == 1) {
            throw new ServiceRuntimeException("uu: " + expectedFirstExceptionCounter)
        }

        expectedFirstExceptionCounter
    }

    class ServiceRuntimeException extends RuntimeException {
        ServiceRuntimeException(String var1) {
            super(var1)
        }
    }

}
