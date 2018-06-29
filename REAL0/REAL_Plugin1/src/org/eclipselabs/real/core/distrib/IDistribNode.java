package org.eclipselabs.real.core.distrib;

import java.util.concurrent.CompletableFuture;

public interface IDistribNode<R,A extends IDistribAccumulator<R,F,E>,F,E> extends
        IDistribNodeFolder<R, A, F, E>,
        IDistribLeafFolder<R, A, F, E> {

    @Override
    default boolean isRoot() {
        return false;
    }

    public CompletableFuture<Void> executeChildren();


}
