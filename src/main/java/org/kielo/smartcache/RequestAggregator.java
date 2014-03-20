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

/**
 *
 * @author Adam Dubiel
 */
class RequestAggregator {

    private final ConcurrentMap<String, Future<?>> aggregator = new ConcurrentHashMap<>();

    private final ExecutorService executor;

    RequestAggregator(ExecutorService executor) {
        this.executor = executor;
    }

    <T> RequestQueueFuture<T> aggregate(String key, final Callable<T> action) {
        return putAndSchedule(key, action);
    }

    <T> RequestQueueFuture<T> syncAggregate(String key, final Callable<T> action) {
        synchronized (aggregator) {
            return putAndSchedule(key, action);
        }
    }

    @SuppressWarnings("unchecked")
    private <T> RequestQueueFuture<T> putAndSchedule(String key, final Callable<T> action) {
        Future<?> future;
        if (!aggregator.containsKey(key)) {
            future = executor.submit(action);
            aggregator.put(key, future);
        } else {
            future = aggregator.get(key);
        }

        return new RequestQueueFuture<T>(this, (Future<T>) future, key);
    }

    void remove(String key) {
        aggregator.remove(key);
    }

    boolean contains(String key) {
        return aggregator.containsKey(key);
    }
}
