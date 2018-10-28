package org.eclipselabs.real.core.distrib;

import java.util.List;

public interface IDistribLeafFolder<R,A extends IDistribAccumulator<R,F,E>,F,E> extends
        IDistribAbstractElement<R, A, F, E>, IDistribFolder {

    public List<IDistribLeaf<R,A,F,E>> getLeafChildren();

    public void addLeafChildren(List<IDistribLeaf<R,A,F,E>> lst);

    public void setLeafChildren(List<IDistribLeaf<R,A,F,E>> lst);

}
