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

public class SmartCacheTest {

    @Test
    public void shouldPutObjectIntoCache() {
        // given
        SmartCache cache = new SmartCache(Executors.newCachedThreadPool());
        cache.registerRegion(new Region("region", new EternalExpirationPolicy(), 5, 1000));

        // when
        cache.put("region", "key", "value");

        // then
        assertThat(cache.get("region", "key", null).result()).isEqualTo("value");
    }

    @Test
    public void shouldNotRunTwoRequestForSameKeyAtTheSameTime() {
        // given
        SmartCache cache = new SmartCache(Executors.newCachedThreadPool());
        cache.registerRegion(new Region("region", new EternalExpirationPolicy(), 5, 1000));
        CountingLongRunningAction action = CountingLongRunningAction.waiting(100);

        // when
        cache.get("region", "key", action);
        cache.get("region", "key", action);

        // then
        assertThat(action.getCounter()).isEqualTo(1);
    }

    @Test
    public void shouldReturnCacheHit() {
        // given
        SmartCache cache = new SmartCache(Executors.newCachedThreadPool());
        cache.registerRegion(new Region("region", new EternalExpirationPolicy(), 5, 1000));
        CountingLongRunningAction action = CountingLongRunningAction.immediate();

        // when
        ActionResult<Integer> first = cache.get("region", "key", action);
        ActionResult<Integer> second = cache.get("region", "key", action);

        // then
        assertThat(first.isFromCache()).isFalse();
        assertThat(second.isFromCache()).isTrue();
    }

    @Test
    public void shouldNotRefreshCacheWhenFreshKeyIsInCache() {
        // given
        SmartCache cache = new SmartCache(Executors.newCachedThreadPool());
        cache.registerRegion(new Region("region", new EternalExpirationPolicy(), 5, 1000));
        CountingLongRunningAction action = CountingLongRunningAction.immediate();
        cache.get("region", "key", action);

        // when
        cache.get("region", "key", action);

        // then
        assertThat(action.getCounter()).isEqualTo(1);
    }

    @Test
    public void shouldRunActionWhenActionInCacheIsExpired() {
        // given
        SmartCache cache = new SmartCache(Executors.newCachedThreadPool());
        cache.registerRegion(new Region("region", new ImmediateExpirationPolicy(), 5, 1000));
        CountingLongRunningAction action = CountingLongRunningAction.immediate();

        // when
        cache.get("region", "key", action);
        cache.get("region", "key", action);

        // then
        assertThat(action.getCounter()).isEqualTo(2);
    }

    @Test
    public void shouldReturnValueFromResolvedAction() {
        // given
        SmartCache cache = new SmartCache(Executors.newCachedThreadPool());
        cache.registerRegion(new Region("region", new EternalExpirationPolicy(), 5, 1000));
        CountingLongRunningAction action = CountingLongRunningAction.immediate();

        // when
        int value = cache.get("region", "key", action).result();

        // then
        assertThat(value).isEqualTo(1);
    }
    
    @Test
    public void shouldNotCacheFailedResult() {
        // given
        SmartCache cache = new SmartCache(Executors.newCachedThreadPool());
        cache.registerRegion(new Region("region", new EternalExpirationPolicy(), 5, 1000));
        CountingLongRunningAction action = CountingLongRunningAction.failingOn(0);


        // when
        ActionResult<Integer> failedResult = cache.get("region", "key", action);
        ActionResult<Integer> result = cache.get("region", "key", action);

        // then
        assertThat(failedResult.caughtException()).isInstanceOf(IllegalStateException.class);
        assertThat(result.result()).isEqualTo(2);
    }
}
