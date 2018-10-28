package org.eclipselabs.real.core.distrib;

import java.util.concurrent.ExecutorService;

public interface IDistribAbstractElement<R, A extends IDistribAccumulator<R,F,E>, F, E> {

    boolean isRoot();

    public IDistribRoot<R,A,F,E> getRoot();

    public ExecutorService getExecutorService();
}
