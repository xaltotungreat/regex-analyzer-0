package org.eclipselabs.real.core.searchobject;

import java.util.List;

import org.eclipselabs.real.core.searchresult.ISRComplexRegexView;
import org.eclipselabs.real.core.searchresult.resultobject.ISROComplexRegexView;

public interface ISOComplexRegexView extends ISearchObject<ISRComplexRegexView,ISROComplexRegexView> {

    public void add(Object viewObject);
    public List<Object> getViewObjects();
    public void setViewObjects(List<Object> newViewObjects);
}
