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

import java.util.concurrent.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Adam Dubiel
 */
public class SmartCache {

    private final static Logger logger = LoggerFactory.getLogger(SmartCache.class);

    private final CacheRegions regions = new CacheRegions();

    private final RequestAggregator requestQueue;

    public SmartCache(ExecutorService executorService) {
        requestQueue = new RequestAggregator(executorService);
    }

    public void registerRegion(Region region) {
        regions.register(region);
    }

    public void put(String regionName, String key, Object object) {
        regions.region(regionName).put(key, object);
    }

    private <T> RequestQueueFuture<T> put(final String regionName, final String key, final Callable<T> action) {
        final String fullKey = Region.key(regionName, key);
        return requestQueue.aggregate(fullKey, new Callable<T>() {
            @Override
            public T call() throws Exception {
                T resolvedObject = action.call();
                regions.region(regionName).put(key, resolvedObject);
                return resolvedObject;
            }
        });
    }

    @SuppressWarnings("unchecked")
    public <T> ActionResult<T> get(String regionName, String key, final Callable<T> action) {
        Region region = regions.region(regionName);
        CacheEntry entry = region.get(key);

        T value = null;
        Throwable caughtException = null;

        if (entry == null || region.expirationPolicy().expire(entry)) {
            try {
                value = put(regionName, key, action).resolve(region.timeout());
            } catch (TimeoutException timeoutException) {
                logger.info("Action timed out after {} milliseconds, returning cached value.", region.timeout());
                caughtException = timeoutException;
            } catch (ActionResolvingException actionException) {
                logger.info("Action failed, returning cached value with exception: {}", actionException.toString());
                caughtException = actionException.getCause();
            } catch (Exception exception) {
                logger.info("Action failed, returning cached value with exception: {} {}", exception.getClass().getSimpleName(), exception.getMessage());
                caughtException = exception;
            }
        }

        if (entry != null && value == null) {
            value = entry.value();
        }

        return new ActionResult<>(value, caughtException);
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
}
