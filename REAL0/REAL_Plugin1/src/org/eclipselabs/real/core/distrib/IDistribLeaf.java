package org.eclipselabs.real.core.distrib;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

public interface IDistribLeaf<R,A extends IDistribAccumulator<R,F,E>,F,E> {

    public default boolean isRoot() {
        return false;
    }

    public IDistribRoot<R,A,F,E> getRoot();

    public CompletableFuture<R> execute();

    public CompletableFuture<R> execute(ExecutorService es);

    public IDistribTask<R> getTask();
}
