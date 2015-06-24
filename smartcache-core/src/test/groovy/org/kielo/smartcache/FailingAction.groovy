package org.kielo.smartcache

import java.util.concurrent.Callable

class FailingAction implements Callable<Void> {
    
    @Override
    Void call() throws Exception {
        throw new IllegalStateException("This action should fail")
    }
}
