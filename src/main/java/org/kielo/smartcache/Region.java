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

/**
 *
 * @author Adam Dubiel
 */
public class Region {

    private static final String REGION_SEPARATOR = "#";

    private final String name;

    private final ConcurrentMap<String, CacheEntry> cache;

    private final ExpirationPolicy expirationPolicy;

    private final int timeout;

    public Region(String name, ExpirationPolicy expirationPolicy, final ActionResultWeigher weigher, int capacity, int actionTimeout) {
        this.name = name;
        this.expirationPolicy = expirationPolicy;
        this.timeout = actionTimeout;

        this.cache = new ConcurrentLinkedHashMap.Builder<String, CacheEntry>()
                .weigher(Weighers.from(weigher))
                .maximumWeightedCapacity(capacity)
                .build();
    }

    public Region(String name, ExpirationPolicy expirationPolicy, int capacity, int timeout) {
        this(name, expirationPolicy, null, capacity, timeout);
    }

    public static String key(String regionName, String key) {
        return regionName + REGION_SEPARATOR + key;
    }

    public String name() {
        return name;
    }

    public ExpirationPolicy expirationPolicy() {
        return expirationPolicy;
    }

    public int timeout() {
        return timeout;
    }

    void put(String key, Object object) {
        cache.put(key, new CacheEntry(object));
    }

    CacheEntry get(String key) {
        return cache.get(key);
    }

    void evict(String key) {
        cache.remove(key);
    }

    void evictAll() {
        cache.clear();
    }
}
