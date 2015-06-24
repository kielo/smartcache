package org.kielo.smartcache

import org.kielo.smartcache.cache.CacheEntry
import org.kielo.smartcache.cache.ExpirationPolicy

class ImmediateExpirationPolicy implements ExpirationPolicy {
    
    @Override
    boolean expire(CacheEntry entry) {
        return true
    }
}
