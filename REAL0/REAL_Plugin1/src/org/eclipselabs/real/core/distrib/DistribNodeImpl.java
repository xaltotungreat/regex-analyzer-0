package org.eclipselabs.real.core.distrib;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

class DistribNodeImpl<R,A extends IDistribAccumulator<R,F,E>,F,E> implements IDistribNode<R, A, F, E> {

    private IDistribNodeFolder<R, A, F, E> parent;

    private ExecutorService executorService;

    private List<IDistribLeaf<R, A, F, E>> leafChildren = Collections.synchronizedList(new ArrayList<>());

    private List<IDistribNode<R, A, F, E>> nodeChildren = Collections.synchronizedList(new ArrayList<>());

    public DistribNodeImpl() {
        // simple empty constructor
    }

    public DistribNodeImpl(IDistribNodeFolder<R, A, F, E> pr) {
        parent = pr;
    }

    public DistribNodeImpl(IDistribNodeFolder<R, A, F, E> pr, ExecutorService exec) {
        this(pr);
        executorService = exec;
    }

    @Override
    public IDistribRoot<R, A, F, E> getRoot() {
        return parent.getRoot();
    }

    @Override
    public ExecutorService getExecutorService() {
        return (executorService == null)?parent.getExecutorService():executorService;
    }

    @Override
    public synchronized List<IDistribLeaf<R, A, F, E>> getLeafChildren() {
        return leafChildren;
    }

    @Override
    public synchronized void addLeafChildren(List<IDistribLeaf<R, A, F, E>> lst) {
        leafChildren.addAll(lst);
    }

    @Override
    public synchronized void setLeafChildren(List<IDistribLeaf<R, A, F, E>> lst) {
        leafChildren.clear();
        leafChildren.addAll(lst);
    }

    @Override
    public synchronized List<IDistribNode<R, A, F, E>> getNodeChildren() {
        return nodeChildren;
    }

    @Override
    public synchronized void addNodeChildren(List<IDistribNode<R, A, F, E>> lst) {
        nodeChildren.addAll(lst);
    }

    @Override
    public synchronized void setNodeChildren(List<IDistribNode<R, A, F, E>> lst) {
        nodeChildren.clear();
        nodeChildren.addAll(lst);
    }

    @Override
    public synchronized CompletableFuture<Void> executeChildren() {
        CompletableFuture<Void>[] subFuturesNodes = new CompletableFuture[nodeChildren.size()];
        for (int i = 0; i < nodeChildren.size(); i++) {
            subFuturesNodes[i] = nodeChildren.get(i).executeChildren();
        }
        CompletableFuture<IDistribTaskResultWrapper<R>>[] subFuturesLeaves = new CompletableFuture[leafChildren.size()];
        for (int i = 0; i < leafChildren.size(); i++) {
            subFuturesLeaves[i] = leafChildren.get(i).execute();
        }
        CompletableFuture<Void> ftNodes = CompletableFuture.allOf(subFuturesNodes);
        CompletableFuture<Void> ftLeaves = CompletableFuture.allOf(subFuturesLeaves);
        return CompletableFuture.allOf(ftNodes, ftLeaves);
    }

    @Override
    public int getLeafCount() {
        int count = 0;
        count += nodeChildren.stream().mapToInt(IDistribNode::getLeafCount).sum();
        count += leafChildren.size();
        return count;
    }

}
