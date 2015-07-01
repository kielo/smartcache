/*
 * Copyright 2014 Adam Dubiel, Przemek Hertel.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.kielo.smartcache;

import org.kielo.smartcache.action.ActionResolvingException;
import org.kielo.smartcache.action.ActionResult;
import org.kielo.smartcache.aggregator.RequestAggregator;
import org.kielo.smartcache.aggregator.RequestQueueFuture;
import org.kielo.smartcache.cache.CacheEntry;
import org.kielo.smartcache.cache.CacheRegions;
import org.kielo.smartcache.cache.Region;
import org.kielo.smartcache.cache.TimeExpirationPolicy;
import org.kielo.smartcache.metrics.MetricsMetadata;
import org.kielo.smartcache.metrics.NoopCacheMetrics;
import org.kielo.smartcache.metrics.CacheMetrics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeoutException;

public class SmartCache {

    private final static Logger logger = LoggerFactory.getLogger(SmartCache.class);

    private static final String DEFAULT_REGION_NAME = "default";
    
    private final CacheRegions regions = new CacheRegions();

    private final RequestAggregator requestQueue;

    private final CacheMetrics metrics;

    public SmartCache(ExecutorService executorService, CacheMetrics metrics) {
        requestQueue = new RequestAggregator(executorService);
        this.metrics = metrics;
        
        this.regions.register(new Region(DEFAULT_REGION_NAME, new TimeExpirationPolicy(1000), 10_000));
    }

    public SmartCache(ExecutorService executorService) {
        this(executorService, new NoopCacheMetrics());
    }
    
    public void registerRegion(Region region) {
        regions.register(region);
    }

    public void put(String regionName, String key, Object object) {
        regions.region(regionName).put(key, object);
    }

    private <T> RequestQueueFuture<T> put(final String regionName, final String key, final Callable<T> action) {
        return requestQueue.aggregate(Region.key(regionName, key), () -> {
            T resolvedObject = action.call();
            regions.region(regionName).put(key, resolvedObject);
            return resolvedObject;
        });
    }

    public <T> InvocationBuilder<T> get(String key, Class<T> classMarker) {
        return new InvocationBuilder<>(this, key);
    }

    public <T> InvocationBuilder<T> get(String key) {
        return new InvocationBuilder<>(this, key);
    }
    
    @SuppressWarnings("unchecked")
    private <T> ActionResult<T> get(String regionName, String key, MetricsMetadata metricsMetadata, Duration timeout, Callable<T> action) {
        Region region = regions.region(regionName);
        CacheEntry entry = region.get(key);

        T value = null;
        Throwable caughtException = null;
        boolean valueFromCache = entry != null;
        
        if (!valueFromCache || region.expirationPolicy().expire(entry)) {
            Object timerContext = metrics.actionResolutionStarted(regionName, key, metricsMetadata);
            try {
                metrics.actionExecuted(regionName, key, metricsMetadata);
                value = put(regionName, key, action).resolve(timeout);
                valueFromCache = false;
            } catch(TimeoutException timeoutException) {
                logger.info("Action timed out after {} milliseconds, returning cached value.", timeout.toMillis());
                caughtException = timeoutException;
                metrics.actionTimeout(regionName, key, metricsMetadata);
            } catch(ActionResolvingException actionException) {
                logger.info("Action failed, returning cached value with exception: {}", actionException.toString());
                caughtException = actionException.getCause();
                metrics.actionError(regionName, key, metricsMetadata);
            } catch(Exception exception) {
                logger.info("Action failed, returning cached value with exception: {} {}", exception.getClass().getSimpleName(), exception.getMessage());
                caughtException = exception;
                metrics.actionError(regionName, key, metricsMetadata);
            } finally {
                metrics.actionResolutionFinished(regionName, key, metricsMetadata, timerContext);
            }
        } else {
            value = entry.value();
            metrics.cacheHit(regionName, key, metricsMetadata);
        }
        
        if (valueFromCache && caughtException != null) {
            value = entry.value();
            metrics.staleCacheHit(regionName, key, metricsMetadata);
        }
        
        return new ActionResult<>(value, caughtException, valueFromCache);
    }

    public void evict(String regionName, String key) {
        regions.region(regionName).evict(key);
    }

    public void evict(String regionName) {
        regions.region(regionName).evictAll();
    }

    public void evict() {
        regions.evict();
    }

    @SuppressWarnings("unchecked")
    public <T extends CacheMetrics> T metrics() {
        return (T) metrics;
    }
    
    public class InvocationBuilder<T> {
        
        private final SmartCache smartCache;
        
        private final String key;
        
        private String region = DEFAULT_REGION_NAME;
        
        private MetricsMetadata metricsMetadata; 
                
        private Duration timeout = Duration.ofMillis(1000);

        private InvocationBuilder(SmartCache smartCache, String key) {
            this.smartCache = smartCache;
            this.key = key;
        }
        
        public ActionResult<T> invoke(Callable<T> action) {
            return smartCache.get(region, key, metricsMetadata, timeout, action);
        }
        
        public InvocationBuilder<T> fromRegion(String region) {
            this.region = region;
            return this;
        }

        public InvocationBuilder<T> withMetricsMetadata(MetricsMetadata metricsMetadata) {
            this.metricsMetadata = metricsMetadata;
            return this;
        }
        
        public InvocationBuilder<T> withTimeout(Duration timeout) {
            this.timeout = timeout;
            return this;
        }

        public InvocationBuilder<T> withTimeout(long millis) {
            this.timeout = Duration.ofMillis(millis);
            return this;
        }
    }
}
