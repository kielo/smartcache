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
class RequestQueue {

    private final ConcurrentMap<String, Future<?>> queue = new ConcurrentHashMap<>();

    private final ExecutorService executor;

    RequestQueue(ExecutorService executor) {
        this.executor = executor;
    }

    <T> RequestQueueFuture<T> enqueue(String key, final QueuedAction<T> action) {
        return putAndSchedule(key, action);
    }

    <T> RequestQueueFuture<T> syncEnqueue(String key, final QueuedAction<T> action) {
        synchronized (queue) {
            return putAndSchedule(key, action);
        }
    }

    @SuppressWarnings("unchecked")
    private <T> RequestQueueFuture<T> putAndSchedule(String key, final QueuedAction<T> action) {
        Future<?> future;
        if (!queue.containsKey(key)) {
            future = executor.submit(new Callable<T>() {
                @Override
                public T call() {
                    return action.resolve();
                }
            });
            queue.put(key, future);
        } else {
            future = queue.get(key);
        }

        return new RequestQueueFuture<T>(this, (Future<T>) future, key);
    }

    void remove(String key) {
        queue.remove(key);
    }
}
