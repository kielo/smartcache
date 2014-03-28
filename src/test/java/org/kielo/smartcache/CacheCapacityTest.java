package org.kielo.smartcache;

import org.assertj.core.api.Assertions;
import org.testng.annotations.Test;

import java.util.concurrent.Executors;

/**
 * @author bartosz walacik
 */
public class CacheCapacityTest {

    @Test
    public void shouldNotExceedGivenCapacity(){
        // given
        final int CAPACITY = 5;
        SmartCache cache = new SmartCache(Executors.newCachedThreadPool());
        cache.registerRegion(new Region("region", new EternalExpirationPolicy(), CAPACITY, 1000));
        CountingLongRunningAction action = CountingLongRunningAction.immediate();

        // when
        // put capacity + 1 items
        for(int i=0; i<=CAPACITY; i++){
            cache.put("region", "key"+i, "value");
        }
        //get oldest
        cache.get("region", "key0", action);

        // then
        // should miss once
        Assertions.assertThat(action.getCounter()).isEqualTo(1);
    }
}
