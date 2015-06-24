package org.kielo.smartcache

import org.kielo.smartcache.cache.CacheEntry
import org.kielo.smartcache.cache.ExpirationPolicy

class EternalExpirationPolicy implements ExpirationPolicy {
    
    @Override
    boolean expire(CacheEntry entry) {
        return false
    }
}
