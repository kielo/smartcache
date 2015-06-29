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
import org.kielo.smartcache.metrics.NoopSmartCacheMetrics;
import org.kielo.smartcache.metrics.SmartCacheMetrics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeoutException;

public class SmartCache {

    private final static Logger logger = LoggerFactory.getLogger(SmartCache.class);

    private final CacheRegions regions = new CacheRegions();

    private final RequestAggregator requestQueue;

    private final SmartCacheMetrics metrics;

    public SmartCache(ExecutorService executorService, SmartCacheMetrics metrics) {
        requestQueue = new RequestAggregator(executorService);
        this.metrics = metrics;
    }

    public SmartCache(ExecutorService executorService) {
        this(executorService, new NoopSmartCacheMetrics());
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

    @SuppressWarnings("unchecked")
    public <T> ActionResult<T> get(String regionName, String key, final Callable<T> action) {
        Region region = regions.region(regionName);
        CacheEntry entry = region.get(key);

        T value = null;
        Throwable caughtException = null;
        boolean fromCache = entry != null;

        if (entry == null || region.expirationPolicy().expire(entry)) {
            Object context = metrics.actionResolutionStarted(regionName, key);
            try {
                metrics.actionExecuted(regionName, key);
                value = put(regionName, key, action).resolve(region.timeout());
                fromCache = false;
            } catch(TimeoutException timeoutException) {
                logger.info("Action timed out after {} milliseconds, returning cached value.", region.timeout());
                caughtException = timeoutException;
                metrics.actionTimeout(regionName, key);
            } catch(ActionResolvingException actionException) {
                logger.info("Action failed, returning cached value with exception: {}", actionException.toString());
                caughtException = actionException.getCause();
                metrics.actionError(regionName, key);
            } catch(Exception exception) {
                logger.info("Action failed, returning cached value with exception: {} {}", exception.getClass().getSimpleName(), exception.getMessage());
                caughtException = exception;
                metrics.actionError(regionName, key);
            } finally {
                metrics.actionResolutionFinished(regionName, key, context);
            }
        } else if (entry != null) {
            value = entry.value();
            metrics.cacheHit(regionName, key);
        }
        if (entry != null && caughtException != null) {
            value = entry.value();
            metrics.staleCacheHit(regionName, key);
        }
        
        return new ActionResult<>(value, caughtException, fromCache);
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
    public <T extends SmartCacheMetrics> T metrics() {
        return (T) metrics;
    }
}
