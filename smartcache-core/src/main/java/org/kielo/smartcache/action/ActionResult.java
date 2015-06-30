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
package org.kielo.smartcache.action;

import java.util.concurrent.TimeoutException;

public class ActionResult<T> {

    private final T result;

    private final Throwable caughtException;

    private final boolean fromCache;

    public ActionResult(T result, Throwable caughtException, boolean fromCache) {
        this.result = result;
        this.caughtException = caughtException;
        this.fromCache = fromCache;
    }

    public static <T> ActionResult<T> success(T result, boolean fromCache) {
        return new ActionResult<>(result, null, fromCache);
    }

    public static <T> ActionResult<T> failed(Throwable cause, boolean fromCache) {
        return new ActionResult<>(null, cause, fromCache);
    }

    public boolean hasResult() {
        return result != null;
    }

    public boolean failed() {
        return caughtException != null;
    }

    public boolean failedWithoutCacheHit() {
        return !hasResult() && failed();
    }
    
    public boolean timeout() {
        return caughtException instanceof TimeoutException;
    }
    
    public T result() {
        return result;
    }

    public Throwable caughtException() {
        return caughtException;
    }

    public boolean isFromCache() {
        return fromCache;
    }
    
    public boolean isFromStaleCache() {
        return hasResult() && isFromCache() && failed();
    }
}
