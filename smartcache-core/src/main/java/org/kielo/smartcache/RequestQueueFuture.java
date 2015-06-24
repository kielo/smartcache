/*
 * Copyright 2014 Adam Dubiel.
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

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class RequestQueueFuture<T> {

    private final RequestAggregator queue;

    private final Future<T> future;

    private final String key;

    RequestQueueFuture(RequestAggregator queue, Future<T> future, String key) {
        this.queue = queue;
        this.future = future;
        this.key = key;
    }

    public T resolve(long maxWaitTime) throws TimeoutException {
        T value = null;
        try {
            value = future.get(maxWaitTime, TimeUnit.MILLISECONDS);
        } catch (ExecutionException exception) {
            throw new ActionResolvingException(exception.getCause());
        } catch (InterruptedException interruptedException) {
            Thread.currentThread().interrupt();
        }
        finally {
            queue.remove(key);
        }

        return value;
    }

}
