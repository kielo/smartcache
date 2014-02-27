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
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

/**
 *
 * @author Adam Dubiel
 */
public class SmartCache {

    private final ConcurrentMap<String, CacheEntry> cache = new ConcurrentLinkedHashMap.Builder<String, CacheEntry>()
            .maximumWeightedCapacity(10)
            .build();

    private final RequestQueue requestQueue;

    private final ExpirationPolicy expirationPolicy;

    public SmartCache(ExecutorService executorService, ExpirationPolicy expirationPolicy) {
        this.requestQueue = new RequestQueue(executorService);
        this.expirationPolicy = expirationPolicy;
    }

    public void put(final String key, final Object object) {

    }

    public <T> Future<T> put(final String key, final CacheableAction<T> action) {
        return requestQueue.enqueue(key, new QueueAction<T>() {
            @Override
            public T resolve() {
                T resolvedObject = action.resolve();
                cache.put(key, new CacheEntry(resolvedObject));
                return resolvedObject;
            }
        });
    }

    @SuppressWarnings("unchecked")
    public <T> T get(String key, final CacheableAction<T> action) {
        CacheEntry entry = cache.get(key);
        if (entry == null || expirationPolicy.expire(entry)) {
            return put(key, action).get();
        }

        return (T) entry.value();
    }

    public void evict(String key) {
        cache.remove(key);
    }

    public void evict() {
        cache.clear();
    }
}
