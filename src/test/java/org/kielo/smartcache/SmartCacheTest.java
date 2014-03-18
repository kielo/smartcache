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

import java.util.concurrent.Executors;
import org.testng.annotations.Test;
import static org.assertj.core.api.Assertions.assertThat;

/**
 *
 * @author Adam Dubiel
 */
public class SmartCacheTest {

    @Test
    public void shouldPutObjectIntoCache() {
        // given
        SmartCache cache = new SmartCache(Executors.newCachedThreadPool(), new EternalExpirationPolicy());

        // when
        cache.put("key", "value");

        // then
        assertThat(cache.get("key", null).result()).isEqualTo("value");
    }

    @Test(groups = "integration")
    public void shouldNotRunTwoRequestForSameKeyAtTheSameTime() {
        // given
        SmartCache cache = new SmartCache(Executors.newCachedThreadPool(), new EternalExpirationPolicy());
        CountingLongRunningAction action = CountingLongRunningAction.waiting(100);

        // when
        cache.get("key", action);
        cache.get("key", action);

        // then
        assertThat(action.getCounter()).isEqualTo(1);
    }

    @Test
    public void shouldNotRefreshCacheWhenFreshKeyIsInCache() {
        // given
        SmartCache cache = new SmartCache(Executors.newCachedThreadPool(), new EternalExpirationPolicy());
        CountingLongRunningAction action = CountingLongRunningAction.immediate();
        cache.get("key", action);

        // when
        cache.get("key", action);

        // then
        assertThat(action.getCounter()).isEqualTo(1);
    }

    @Test
    public void shouldRunActionWhenActionInCacheIsExpired() {
        // given
        SmartCache cache = new SmartCache(Executors.newCachedThreadPool(), new ImmediateExpirationPolicy());
        CountingLongRunningAction action = CountingLongRunningAction.immediate();

        // when
        cache.get("key", action);
        cache.get("key", action);

        // then
        assertThat(action.getCounter()).isEqualTo(2);
    }

    @Test
    public void shouldReturnValueFromResolvedAction() {
        // given
        SmartCache cache = new SmartCache(Executors.newCachedThreadPool(), new EternalExpirationPolicy());
        CountingLongRunningAction action = CountingLongRunningAction.immediate();

        // when
        int value = cache.get("key", action).result();

        // then
        assertThat(value).isEqualTo(1);
    }
}
