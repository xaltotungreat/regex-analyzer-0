package org.eclipselabs.real.core.searchobject;

import java.util.List;
import java.util.Map;

import org.eclipselabs.real.core.searchresult.IComplexSearchResult;
import org.eclipselabs.real.core.searchresult.ISearchResult;
import org.eclipselabs.real.core.searchresult.resultobject.IComplexSearchResultObject;
import org.eclipselabs.real.core.searchresult.resultobject.ISearchResultObject;

public interface IComplexSearchObject<R extends IComplexSearchResult<O,W,X,Q>,
            O extends IComplexSearchResultObject<W,X,Q>,
            V extends ISearchObject<W,X>,W extends ISearchResult<X>, X extends ISearchResultObject,Q>
        extends ISearchObject<R,O> {

    public void addView(Q viewName, V newView);
    public boolean addView(Q viewName, V newView, Integer viewOrderPos);
    public boolean setView(Q viewName, V newView, Integer viewOrderPos);
    public V getView(Q viewName);
    public V getView(int pos);
    public V removeView(Q viewName);
    public V removeView(int pos);
    public Map<Q, V> getViewMap();
    public int getViewCount();
    public void setViewMap(Map<Q, V> aViewMap);
    public void setViewMapWithOrder(Map<Q, V> aViewMap, List<Q> aViewOrder);
    public void setViewOrder(List<Q> aViewOrder);
    public Q getViewKey(int pos);

}
