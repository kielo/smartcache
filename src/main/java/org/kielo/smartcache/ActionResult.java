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

/**
 *
 * @author Adam Dubiel
 */
public class ActionResult<T> {

    private final T result;

    private final Throwable caughtException;

    public ActionResult(T result, Throwable caughtException) {
        this.result = result;
        this.caughtException = caughtException;
    }

    public static <T> ActionResult<T> success(T result) {
        return new ActionResult<>(result, null);
    }

    public static <T> ActionResult<T> failed(Throwable cause) {
        return new ActionResult<>(null, cause);
    }

    public boolean hasResult() {
        return result != null;
    }

    public boolean failed() {
        return caughtException != null;
    }

    public T result() {
        return result;
    }

    public Throwable caughtException() {
        return caughtException;
    }

}