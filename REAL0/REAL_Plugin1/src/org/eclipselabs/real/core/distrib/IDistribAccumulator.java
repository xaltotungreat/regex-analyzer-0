package org.eclipselabs.real.core.distrib;

import java.util.List;

public interface IDistribAccumulator<R, E> {

    public void addResult(R newResult);

    public List<R> getAllResults();

    public void addError(E newError);

    public List<E> getErrors();
}
