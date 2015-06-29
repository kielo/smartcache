package org.kielo.smartcache.spring;

import com.codahale.metrics.MetricRegistry;
import org.kielo.smartcache.SmartCache;
import org.kielo.smartcache.cache.Region;
import org.kielo.smartcache.cache.TimeExpirationPolicy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

import java.util.concurrent.Executors;

/**
 * @author bartosz walacik
 */
@Configuration
@ComponentScan(basePackages = "org.kielo.smartcache.spring")
@EnableAspectJAutoProxy
public class SmartCacheAspectTestConfig {

    @Bean
    SomeBean someBean() {
        return new SomeBean();
    }

    @Bean
    public SmartCache smartCache() {
        SmartCache cache = new org.kielo.smartcache.SmartCache(Executors.newCachedThreadPool());
        cache.registerRegion(new Region("standardCache", new TimeExpirationPolicy(1000), 50_000, 3_000));
        cache.registerRegion(new Region("zeroCache", new TimeExpirationPolicy(0), 50_000, 3_000));
        return cache;
    }

    @Bean
    public MetricRegistry metricRegistry(){
        return new MetricRegistry();
    }
}
