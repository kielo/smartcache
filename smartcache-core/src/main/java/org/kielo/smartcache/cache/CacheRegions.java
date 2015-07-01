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
package org.kielo.smartcache.cache;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class CacheRegions {

    private final Map<String, Region> regions = new ConcurrentHashMap<>();

    public CacheRegions() {
    }

    public void register(Region region) {
        regions.put(region.name(), region);
    }

    public Region region(String name) {
        return regions.computeIfAbsent(name, n -> {
            throw new RegionNotDefinedException(n);
        });
    }

    public void evict() {
        regions.values().forEach(Region::evictAll);
    }
}
