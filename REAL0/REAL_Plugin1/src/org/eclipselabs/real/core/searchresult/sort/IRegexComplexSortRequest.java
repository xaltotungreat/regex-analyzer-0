package org.eclipselabs.real.core.searchresult.sort;

public interface IRegexComplexSortRequest<Q> extends IRegexSortRequest {

    public Q getViewName();
    public void setViewName(Q viewName);
}
