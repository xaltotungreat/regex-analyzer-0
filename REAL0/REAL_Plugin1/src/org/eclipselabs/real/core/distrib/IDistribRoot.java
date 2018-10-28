package org.eclipselabs.real.core.distrib;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.eclipselabs.real.core.util.TimeUnitWrapper;

public interface IDistribRoot<R, A extends IDistribAccumulator<R,F,E>, F, E> extends
        IDistribAbstractElement<R, A, F, E>,
        IDistribNodeFolder<R, A, F, E>,
        AutoCloseable {

    @Override
    default boolean isRoot() {
        return true;
    }

    @Override
    default IDistribRoot<R,A,F,E> getRoot() {
        return this;
    }

    void addLockingParams(Runnable lockFn, Runnable unlockFn);

    void setExecutionTimeout(TimeUnitWrapper executionTimeout);

    public void setAfterLockRun(List<Runnable> afterLockRunSync, List<Runnable> afterLockRunAsync);

    public void setAfterExecRun(List<Runnable> afterExecRunSync, List<Runnable> afterExecRunAsync);

    int getTotalTasks();

    CompletableFuture<A> execute();

    public A getAccumulator();

}
