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

/**
 *
 * @author Adam Dubiel
 */
public class CountingLongRunningAction implements CacheableAction<Integer> {

    private final int waitDuration;

    private final boolean wait;

    private volatile int counter = 0;

    private CountingLongRunningAction(int waitDuration) {
        this.waitDuration = waitDuration;
        this.wait = true;
    }

    private CountingLongRunningAction() {
        this.waitDuration = 0;
        this.wait = false;
    }

    public static CountingLongRunningAction waiting(int waitDuration) {
        return new CountingLongRunningAction(waitDuration);
    }

    public static CountingLongRunningAction immediate() {
        return new CountingLongRunningAction();
    }

    @Override
    public synchronized Integer resolve() {
        counter++;
        if (wait) {
            try {
                this.wait(waitDuration);
            } catch (InterruptedException exception) {
                throw new IllegalStateException(exception);
            }
        }
        return counter;
    }

    public int getCounter() {
        return counter;
    }
}
