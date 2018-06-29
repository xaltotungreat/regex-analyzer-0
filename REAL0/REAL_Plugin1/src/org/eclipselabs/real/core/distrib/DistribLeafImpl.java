package org.eclipselabs.real.core.distrib;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

public class DistribLeafImpl<R,A extends IDistribAccumulator<R,F,E>,F,E> implements IDistribLeaf<R,A,F,E> {

    private IDistribTask<R> task;

    private IDistribLeafFolder<R,A,F,E> parent;

    private IDistribAccumulator<R, F, GenericError> accumulator;

    public DistribLeafImpl() {
        // a simple empty constructor
    }

    public DistribLeafImpl(IDistribLeafFolder<R,A,F,E> pr, IDistribTask<R> ts, IDistribAccumulator<R, F, GenericError> acc) {
        parent = pr;
        task = ts;
        accumulator = acc;
    }

    @Override
    public CompletableFuture<R> execute() {
        return execute(parent.getExecutorService());
    }

    @Override
    public CompletableFuture<R> execute(ExecutorService es) {
        CompletableFuture<R> execFuture = CompletableFuture.supplyAsync(task, es);
        return execFuture.handle((R arg0, Throwable t) -> {
            if (arg0 != null) {
                accumulator.addResult(arg0);
            }
            if (t != null) {
                accumulator.addError(new GenericError(t));
            }
            return arg0;
        });
    }

    @Override
    public IDistribTask<R> getTask() {
        return task;
    }

    @Override
    public IDistribRoot<R,A,F,E> getRoot() {
        return parent.getRoot();
    }

}
