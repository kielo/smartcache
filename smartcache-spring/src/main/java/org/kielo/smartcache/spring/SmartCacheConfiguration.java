package org.kielo.smartcache.spring;

import org.kielo.smartcache.SmartCache;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SmartCacheConfiguration {
    
    @Bean
    public SmartCacheAspect smartCacheAspect(SmartCache smartCache) {
        return new SmartCacheAspect(smartCache);
    }
    
}
