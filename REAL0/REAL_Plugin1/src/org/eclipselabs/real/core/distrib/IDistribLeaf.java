package org.eclipselabs.real.core.distrib;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

public interface IDistribLeaf<R,A extends IDistribAccumulator<R,F,E>,F,E> {

    public default boolean isRoot() {
        return false;
    }

    public IDistribRoot<R,A,F,E> getRoot();

    public CompletableFuture<IDistribTaskResultWrapper<R>> execute();

    public CompletableFuture<IDistribTaskResultWrapper<R>> execute(ExecutorService es);

    public IDistribTask<R> getTask();
}
