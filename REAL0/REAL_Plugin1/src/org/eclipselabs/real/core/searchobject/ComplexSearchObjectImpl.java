package org.eclipselabs.real.core.searchobject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipselabs.real.core.searchresult.IComplexSearchResult;
import org.eclipselabs.real.core.searchresult.ISearchResult;
import org.eclipselabs.real.core.searchresult.resultobject.IComplexSearchResultObject;
import org.eclipselabs.real.core.searchresult.resultobject.ISearchResultObject;

public abstract class ComplexSearchObjectImpl<R extends IComplexSearchResult<O,W,X,Q>,
            O extends IComplexSearchResultObject<W,X,Q>,
            V extends ISearchObject<W,X>,W extends ISearchResult<X>, X extends ISearchResultObject,Q> 
        extends SearchObjectImpl<R,O> implements IComplexSearchObject<R,O,V,W,X,Q>{
    
    private static final Logger log = LogManager.getLogger(ComplexSearchObjectImpl.class);
    protected Map<Q,V> viewMap = new ConcurrentHashMap<Q,V>();
    protected List<Q> viewOrder = Collections.synchronizedList(new ArrayList<Q>());
    
    public ComplexSearchObjectImpl(SearchObjectType aType, String aName) {
        super(aType, aName);
    }
    
    @Override
    public void addView(Q viewName, V newView) {
        viewMap.put(viewName, newView);
        viewOrder.add(viewName);
    }

    @Override
    public void addView(Q viewName, V newView, Integer viewOrderPos) {
        viewMap.put(viewName, newView);
        if (viewOrderPos != null) {
            if ((viewOrderPos > 0) && (viewOrderPos < viewOrder.size())) {
                viewOrder.add(viewOrderPos, viewName);
            } else {
                log.error("addView incorrect order position viewOrderPos=" + viewOrderPos + " size=" + viewOrder.size());
            }
        } else {
            viewOrder.add(viewName);
        }
    }

    @Override
    public V getView(Q viewName) {
        return viewMap.get(viewName);
    }

    @Override
    public V removeView(Q viewName) {
        viewOrder.remove(viewName);
        return viewMap.remove(viewName);
    }

    @Override
    public Map<Q, V> getViewMap() {
        return viewMap;
    }

    @Override
    public int getViewCount() {
        return viewMap.size();
    }

    @Override
    public void setViewMap(Map<Q, V> aViewMap) {
        viewMap.clear();
        viewMap.putAll(aViewMap);
    }

    @Override
    public void setViewMapWithOrder(Map<Q, V> aViewMap, List<Q> aViewOrder) {
        viewMap.clear();
        viewMap.putAll(aViewMap);
        viewOrder.clear();
        viewOrder.addAll(aViewOrder);
    }

    @Override
    public void setViewOrder(List<Q> aViewOrder) {
        viewOrder.clear();
        viewOrder.addAll(aViewOrder);
    }



}
