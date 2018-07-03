package org.eclipselabs.real.core.distrib;

import java.util.concurrent.CompletableFuture;

import org.eclipselabs.real.core.exception.LockTimeoutException;
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

    int getTotalTasks();

    CompletableFuture<A> execute() throws LockTimeoutException;

}
