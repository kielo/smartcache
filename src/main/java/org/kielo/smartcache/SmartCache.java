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

import com.googlecode.concurrentlinkedhashmap.ConcurrentLinkedHashMap;
import java.util.concurrent.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Adam Dubiel
 */
public class SmartCache {

    private final static Logger logger = LoggerFactory.getLogger(SmartCache.class);

    private static final int DEFAULT_TIMEOUT = 1000;

    private final ConcurrentMap<String, CacheEntry> cache = new ConcurrentLinkedHashMap.Builder<String, CacheEntry>()
            .maximumWeightedCapacity(10)
            .build();

    private final RequestAggregator requestQueue;

    private final int actionTimeout;

    private final ExpirationPolicy expirationPolicy;

    public SmartCache(ExecutorService executorService, ExpirationPolicy expirationPolicy) {
        this(executorService, expirationPolicy, DEFAULT_TIMEOUT);
    }

    /**
     * @param actionTimeout max time to wait for action result in millis.
     *                      When exceeded, client gets value from cache (if any)
     */
    public SmartCache(ExecutorService executorService, ExpirationPolicy expirationPolicy, int actionTimeout) {
        this.actionTimeout = actionTimeout;
        this.requestQueue = new RequestAggregator(executorService);
        this.expirationPolicy = expirationPolicy;
    }

    public void put(final String key, final Object object) {
        cache.put(key, new CacheEntry(object));
    }

    private <T> RequestQueueFuture<T> put(final String key, final Callable<T> action) {
        return requestQueue.aggregate(key, new Callable<T>() {
            @Override
            public T call() throws Exception {
                T resolvedObject = action.call();
                cache.put(key, new CacheEntry(resolvedObject));
                return resolvedObject;
            }
        });
    }

    @SuppressWarnings("unchecked")
    public <T> ActionResult<T> get(String key, final Callable<T> action) {
        CacheEntry entry = cache.get(key);
        T value = null;
        Throwable caughtException = null;

        if (entry == null || expirationPolicy.expire(entry)) {
            try {
                value = put(key, action).resolve(actionTimeout);
            } catch (TimeoutException timeoutException) {
                logger.info("Action timed out after {} milliseconds, returning cached value", actionTimeout);
                caughtException = timeoutException;
            } catch (Throwable throwable) {
                logger.info("Action bailed, returning cached value", throwable);
                caughtException = throwable;
            }
        }

        if (entry != null && value == null) {
            value = entry.value();
        }

        return new ActionResult<>(value, caughtException);
    }

    public void evict(String key) {
        cache.remove(key);
    }

    public void evict() {
        cache.clear();
    }
}
