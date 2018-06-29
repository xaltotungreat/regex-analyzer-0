package org.eclipselabs.real.core.distrib;

import java.util.List;

public interface IDistribAccumulator<R, F, E> {

    public void addResult(R newResult);

    public List<R> getAllResults();

    public F getResult();

    public void addError(E newError);

    public List<E> getErrors();
}
