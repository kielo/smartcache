package org.kielo.smartcache;

import java.util.concurrent.Callable;

public class FailingAction implements Callable<Void> {
    
    @Override
    public Void call() throws Exception {
        throw new IllegalStateException("This action should fail");
    }
}
