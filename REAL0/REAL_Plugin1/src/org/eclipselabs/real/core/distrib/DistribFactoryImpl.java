package org.eclipselabs.real.core.distrib;

class DistribFactoryImpl implements IDistribFactory {

    public DistribFactoryImpl() {
        // empty constructor
    }

    @Override
    public <R, A extends IDistribAccumulator<R, F, E>, F, E> IDistribRoot<R, A, F, E> getRoot(A acc, int threadNum, String threadName) {
        return new DistribNodeRoot<>(acc, threadNum, threadName);
    }

    @Override
    public <R, A extends IDistribAccumulator<R, F, E>, F, E> IDistribNode<R, A, F, E> getNode(IDistribNodeFolder<R, A, F, E> pr) {
        return new DistribNodeImpl<>(pr);
    }

    @Override
    public <R, A extends IDistribAccumulator<R, F, E>, F, E> IDistribLeaf<R, A, F, E> getLeaf(IDistribLeafFolder<R, A, F, E> pr, IDistribTask<R> ts, IDistribAccumulator<R, F, GenericError> acc) {
        return new DistribLeafImpl<>(pr, ts, acc);
    }

    @Override
    public <R> IDistribTaskResultWrapper<R> getTaskResult() {
        return new DistribTaskResult<>();
    }

}
