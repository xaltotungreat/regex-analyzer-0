package org.eclipselabs.real.core.distrib;

import java.util.List;

public interface IDistribAccumulator<R, F, E> {

    public void addResult(IDistribTaskResultWrapper<R> newResult);

    public List<IDistribTaskResultWrapper<R>> getAllResults();

    public F getResult();

    public void addError(E newError);

    public List<E> getErrors();
}
