package org.eclipselabs.real.core.distrib;

import java.util.List;

public interface IDistribNodeFolder<R,A extends IDistribAccumulator<R,F,E>,F,E> extends
        IDistribAbstractElement<R, A, F, E>, IDistribFolder {

    public List<IDistribNode<R,A,F,E>> getNodeChildren();

    public void addNodeChildren(List<IDistribNode<R,A,F,E>> lst);

    public void setNodeChildren(List<IDistribNode<R,A,F,E>> lst);
}
