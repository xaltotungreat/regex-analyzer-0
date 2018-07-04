package org.eclipselabs.real.core.distrib;

public interface IDistribFactory {

    <R, A extends IDistribAccumulator<R,F,E>, F, E> IDistribRoot<R, A, F, E> getRoot(A acc, int threadNum, String threadName);

    <R, A extends IDistribAccumulator<R,F,E>, F, E> IDistribNode<R, A, F, E> getNode(IDistribNodeFolder<R, A, F, E> pr);

    <R, A extends IDistribAccumulator<R,F,E>, F, E> IDistribLeaf<R, A, F, E> getLeaf(IDistribLeafFolder<R,A,F,E> pr, IDistribTask<R> ts, IDistribAccumulator<R, F, GenericError> acc);

    <R> IDistribTaskResultWrapper<R> getTaskResult();
}
