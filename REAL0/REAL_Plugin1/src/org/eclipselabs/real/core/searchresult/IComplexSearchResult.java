package org.eclipselabs.real.core.searchresult;

import java.util.List;
import java.util.Map;

import org.eclipselabs.real.core.searchresult.resultobject.IComplexSearchResultObject;
import org.eclipselabs.real.core.searchresult.resultobject.ISearchResultObject;

public interface IComplexSearchResult<O extends IComplexSearchResultObject<W,X,Q>,
        W extends ISearchResult<X>, X extends ISearchResultObject,Q> extends ISearchResult<O> {

    public static final String DEFAULT_SORT_REGEX = "Default";

    public List<String> getViewText(Q aViewName);
    public List<W> getView(Q aViewName);
    public List<Q> getViewKeys();
    public void setViewOrder(List<Q> aViewOrder);
    public List<Q> getViewOrder();

    public List<Map<Q, String>> getViewsText();
    public List<Map<Q,W>> getViews();

    public boolean viewsAvailable();

}
