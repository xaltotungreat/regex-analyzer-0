package org.eclipselabs.real.core.searchresult.resultobject;

import java.util.List;
import java.util.Map;

import org.eclipselabs.real.core.searchresult.ISearchResult;

public interface IComplexSearchResultObject<W extends ISearchResult<X>, X extends ISearchResultObject,Q> extends ISearchResultObject {
    public Map<Q,W> getViews();
    public void setViews(Map<Q,W> newMap);
    public Map<Q,String> getViewsText();
    public Map<Q,List<String>> getViewsTextList();

    public void addView(Q aViewName, W aViewObj);
    public W getView(Q aViewName);
    public String getViewText(Q aViewName);
    public List<String> getViewTextList(Q aViewName);
    public List<Q> getViewKeys();

    public boolean viewsAvailable();

    public void removeView(Q aViewName);
    public void removeViews(List<Q> aViewNames);
}
