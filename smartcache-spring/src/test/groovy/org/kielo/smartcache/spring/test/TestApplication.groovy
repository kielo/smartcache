package org.kielo.smartcache.spring.test

import org.kielo.smartcache.SmartCache
import org.kielo.smartcache.cache.Region
import org.kielo.smartcache.cache.TimeExpirationPolicy
import org.kielo.smartcache.metrics.SimpleCacheMetrics
import org.kielo.smartcache.spring.SmartCacheConfiguration
import org.springframework.context.annotation.*

import java.util.concurrent.Executors

@Configuration
@EnableAspectJAutoProxy
@ComponentScan
@Import(SmartCacheConfiguration.class)
class TestApplication {
    
    @Bean
    public SmartCache smartCache() {
        SmartCache cache = new SmartCache(Executors.newCachedThreadPool(), new SimpleCacheMetrics())
        cache.registerRegion(new Region("standardCache", new TimeExpirationPolicy(1000), 50_000, 3_000))
        cache.registerRegion(new Region("impatientCache", new TimeExpirationPolicy(0), 50_000, 3_000))
        return cache
    }
    
}
