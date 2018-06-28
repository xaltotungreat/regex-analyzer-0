package org.eclipselabs.real.core.distrib;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

public interface IDistribLeaf<R> extends IDistribAbstract<R> {

    @Override
    public IDistribRoot<R> getRoot();

    public CompletableFuture<R> execute();

    public CompletableFuture<R> execute(ExecutorService es);

    public IDistribTask<R> getTask();
}
