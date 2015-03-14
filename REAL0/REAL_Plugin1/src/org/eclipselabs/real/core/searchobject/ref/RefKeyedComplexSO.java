package org.eclipselabs.real.core.searchobject.ref;

import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipselabs.real.core.searchobject.IKeyedComplexSearchObject;
import org.eclipselabs.real.core.searchobject.ISearchObject;
import org.eclipselabs.real.core.searchobject.ISearchObjectGroup;
import org.eclipselabs.real.core.searchobject.SearchObjectType;
import org.eclipselabs.real.core.searchresult.IKeyedComplexSearchResult;
import org.eclipselabs.real.core.searchresult.ISearchResult;
import org.eclipselabs.real.core.searchresult.resultobject.IComplexSearchResultObject;
import org.eclipselabs.real.core.searchresult.resultobject.ISearchResultObject;

public abstract class RefKeyedComplexSO<
            V extends ISearchObject<W,X>, W extends ISearchResult<X>, X extends ISearchResultObject, Q,
            T extends IKeyedComplexSearchObject<? extends IKeyedComplexSearchResult<? extends IComplexSearchResultObject<W, X, Q>, W, X, Q>, 
            ? extends IComplexSearchResultObject<W, X, Q>, V, W, X, Q>>
        extends RefKeyedSO<T> {

    private static final Logger log = LogManager.getLogger(RefKeyedComplexSO.class);
    protected List<RefView<V,W,X,Q>> refViewList;
    
    public RefKeyedComplexSO(RefType aType, SearchObjectType soType, String aName) {
        super(aType, soType, aName);
    }
    
    public RefKeyedComplexSO(SearchObjectType soType, String aName) {
        super(soType, aName);
    }

    public RefKeyedComplexSO(SearchObjectType soType, String aName, ISearchObjectGroup<String> aGroup, Map<String, String> aTags) {
        super(soType, aName, aGroup, aTags);
    }
    
    public RefKeyedComplexSO(RefType aType, SearchObjectType soType, String aName, ISearchObjectGroup<String> aGroup, Map<String, String> aTags) {
        super(aType, soType, aName, aGroup, aTags);
    }

    @Override
    public boolean matchByParameters(T obj) {
        boolean matches = super.matchByParameters(obj);
        if (matches && (refViewList != null) && (!refViewList.isEmpty())) {
            for (RefView<V, W, X, Q> refView : refViewList) {
                if (RefType.MATCH.equals(refView.getType())) {
                    if ((refView.getViewKey() == null) || (refView.getViewSearchObject() == null)) {
                        log.error("matchByParameters one of the values is null (cannot process this param) key=" + refView.getViewKey() 
                                + " search object=" + refView.getViewSearchObject());
                        continue;
                    }
                    V tmpView = obj.getView(refView.getViewKey());
                    if (tmpView == null) {
                        log.debug("matchByParameters view not matched " + refView.getViewKey() + " obj name " + obj.getSearchObjectName());
                        matches = false;
                        break;
                    }
                }
            }
        }
        return matches;
    }
    
    @Override
    public Integer addParameters(T obj) {
        Integer count = super.addParameters(obj);
        if ((refViewList != null) && (!refViewList.isEmpty())) {
            for (RefView<V, W, X, Q> refView : refViewList) {
                if (RefType.ADD.equals(refView.getType())) {
                    if ((refView.getViewKey() == null) || (refView.getViewSearchObject() == null)) {
                        log.error("addParameters one of the values is null (cannot process this param) key=" + refView.getViewKey() 
                                + " search object=" + refView.getViewSearchObject());
                        continue;
                    }
                    if (obj.getView(refView.getViewKey()) == null) {
                        obj.addView(refView.getViewKey(), refView.getViewSearchObject(), refView.getPosition());
                        count++;
                    } else {
                        log.error("addParameters View ADD already exists " + refView);
                    }
                }
            }
        }
        return count;
    }

    @Override
    public Integer replaceAddParameters(T obj) {
        Integer count = super.replaceAddParameters(obj);
        if ((refViewList != null) && (!refViewList.isEmpty())) {
            for (RefView<V, W, X, Q> refView : refViewList) {
                if (RefType.REPLACE_ADD.equals(refView.getType())) {
                    if ((refView.getViewKey() == null) || (refView.getViewSearchObject() == null)) {
                        log.error("replaceAddParameters one of the values is null (cannot process this param) key=" + refView.getViewKey() 
                                + " search object=" + refView.getViewSearchObject());
                        continue;
                    }
                    obj.addView(refView.getViewKey(), refView.getViewSearchObject(), refView.getPosition());
                    count++;
                }
            }
        }
        return count;
    }

    @Override
    public Integer replaceParameters(T obj) {
        Integer count = super.replaceParameters(obj);
        if ((refViewList != null) && (!refViewList.isEmpty())) {
            for (RefView<V, W, X, Q> refView : refViewList) {
                if (RefType.REPLACE.equals(refView.getType())) {
                    if ((refView.getViewKey() == null) || (refView.getViewSearchObject() == null)) {
                        log.error("replaceAddParameters one of the values is null (cannot process this param) key=" + refView.getViewKey() 
                                + " search object=" + refView.getViewSearchObject());
                        continue;
                    }
                    if (obj.getView(refView.getViewKey()) != null) {
                        obj.addView(refView.getViewKey(), refView.getViewSearchObject(), refView.getPosition());
                        count++;
                    } else {
                        log.error("replaceParameters View REPLACE doesn't exist " + refView);
                    }
                }
            }
        }
        return count;
    }

    @Override
    public Integer removeParameters(T obj) {
        Integer count = super.removeParameters(obj);
        if ((refViewList != null) && (!refViewList.isEmpty())) {
            for (RefView<V, W, X, Q> refView : refViewList) {
                if (RefType.REMOVE.equals(refView.getType())) {
                    if (refView.getViewKey() == null) {
                        log.error("removeParameters one of the values is null (cannot process this param) key=" + refView.getViewKey());
                        continue;
                    }
                    obj.removeView(refView.getViewKey());
                    count++;
                }
            }
        }
        return count;
    }

    public void addRefView(RefView<V,W,X,Q> newView) {
        if (refViewList != null) {
            refViewList.add(newView);
        }
    }

    public List<RefView<V, W, X, Q>> getRefViewList() {
        return refViewList;
    }

    public void setRefViewList(List<RefView<V, W, X, Q>> viewList) {
        this.refViewList = viewList;
    }

}
