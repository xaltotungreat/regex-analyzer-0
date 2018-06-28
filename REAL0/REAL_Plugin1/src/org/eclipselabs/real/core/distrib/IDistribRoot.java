package org.eclipselabs.real.core.distrib;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

public interface IDistribRoot<R> extends IDistribAbstract<R> {

    public ExecutorService getDefaultExecutor();

    public int getDefaultThreadsNumber();

    public CompletableFuture<List<R>> execute();

}
