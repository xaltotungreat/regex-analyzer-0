package org.eclipselabs.real.core.distrib;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

public interface IDistribNode<R> extends IDistribAbstract<R> {

    public List<IDistribNode<R>> getNodeChildren();

    public List<IDistribLeaf<R>> getLeafChildren();

    public CompletableFuture<List<R>> executeChildren();

    public ExecutorService getExecutorService();
}
