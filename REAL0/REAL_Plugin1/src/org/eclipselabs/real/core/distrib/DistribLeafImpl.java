package org.eclipselabs.real.core.distrib;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

public class DistribLeafImpl<R> implements IDistribLeaf<R> {

    private IDistribTask<R> task;

    private IDistribNode<R> parent;

    private IDistribAccumulator<R, GenericError> accumulator;

    public DistribLeafImpl() {

    }

    public DistribLeafImpl(IDistribTask<R> ts, IDistribAccumulator<R, GenericError> acc) {
        task = ts;
        accumulator = acc;
    }

    @Override
    public CompletableFuture<R> execute() {
        ExecutorService srv = parent.getExecutorService();
        if (srv == null) {
            srv = getRoot().getDefaultExecutor();
        }
        return execute(srv);
    }

    @Override
    public CompletableFuture<R> execute(ExecutorService es) {
        CompletableFuture<R> leafFuture = CompletableFuture.supplyAsync(task, es);
        leafFuture.handle((R arg0, Throwable t) -> {
            if (arg0 != null) {
                accumulator.addResult(arg0);
            }
            if (t != null) {
                accumulator.addError(new GenericError(t));
            }
            return null;
        });
        return leafFuture;
    }

    @Override
    public IDistribTask<R> getTask() {
        return task;
    }

    @Override
    public IDistribRoot<R> getRoot() {
        return parent.getRoot();
    }

}
