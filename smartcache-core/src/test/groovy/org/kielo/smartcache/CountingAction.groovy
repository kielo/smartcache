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

package org.kielo.smartcache

import java.util.concurrent.Callable

class CountingAction implements Callable<Integer> {

    private final long waitDuration;

    private final boolean wait;

    private final List<Integer> failingIterations;

    private volatile int counter = 0;

    private CountingAction(long waitDuration, Integer... failingIterations) {
        this.waitDuration = waitDuration
        this.wait = waitDuration > 0
        this.failingIterations = Arrays.asList(failingIterations)
    }

    private CountingAction() {
        this(0)
    }

    public static CountingAction waiting(int waitDuration) {
        return new CountingAction(waitDuration)
    }

    public static CountingAction immediate() {
        return new CountingAction()
    }

    public static CountingAction failingOn(Integer... iterations) {
        return new CountingAction(0, iterations)
    }

    public static CountingAction failImmediately() {
        return new CountingAction(0, 0)
    }

    @Override
    public synchronized Integer call() {
        counter++
        if(failingIterations.contains(counter - 1)) {
            throw new IllegalStateException("This action wanted to fail on $counter iteration")
        }

        if (wait) {
            try {
                this.wait(waitDuration)
            } catch (InterruptedException exception) {
                throw new IllegalStateException(exception)
            }
        }
        return counter
    }

    public int getCounter() {
        return counter
    }
}

