package org.eclipselabs.real.core.searchresult;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipselabs.real.core.searchobject.param.IReplaceParam;
import org.eclipselabs.real.core.searchobject.param.ReplaceParamKey;
import org.eclipselabs.real.core.searchresult.resultobject.IComplexSearchResultObject;
import org.eclipselabs.real.core.searchresult.resultobject.ISearchResultObject;
import org.eclipselabs.real.core.searchresult.sort.IInternalSortRequest;

public abstract class ComplexSearchResultImpl<O extends IComplexSearchResultObject<W,X,Q>,
            W extends ISearchResult<X>, X extends ISearchResultObject,Q> extends SearchResultImpl<O> implements
        IComplexSearchResult<O,W,X,Q> {

    private static final Logger log = LogManager.getLogger(ComplexSearchResultImpl.class);

    protected List<Q> viewOrder = new ArrayList<Q>();

    public ComplexSearchResultImpl(String aSOName) {
        super(aSOName);
    }

    public ComplexSearchResultImpl(String aSOName, List<IInternalSortRequest> aSortRequestList) {
        super(aSOName, aSortRequestList);
    }

    public ComplexSearchResultImpl(String aSOName, List<IInternalSortRequest> aSortRequestList, List<O> initSRList) {
        super(aSOName, aSortRequestList, initSRList);
    }

    public ComplexSearchResultImpl(String aSOName, List<IInternalSortRequest> aSortRequestList, Map<String,String> aCachedReplaceTable) {
        super(aSOName, aSortRequestList, aCachedReplaceTable);
    }

    public ComplexSearchResultImpl(String aSOName, List<IInternalSortRequest> aSortRequestList, Map<String,String> aReplaceTable,
            Map<ReplaceParamKey, IReplaceParam<?>> customParams, Map<ReplaceParamKey, IReplaceParam<?>> allParams) {
        super(aSOName, aSortRequestList, aReplaceTable, customParams, allParams);
    }

    public ComplexSearchResultImpl(String aSOName, List<IInternalSortRequest> aSortRequestList,
            Map<String,String> aCachedReplaceTable, List<O> initSRList) {
        super(aSOName, aSortRequestList, aCachedReplaceTable, initSRList);
    }

    public ComplexSearchResultImpl(IComplexSearchResult<O,W,X,Q> copyObj) {
        super(copyObj);
        viewOrder = new ArrayList<Q>(copyObj.getViewOrder());
    }

    @Override
    public List<String> getViewText(Q aViewName) {
        List<String> viewText = new ArrayList<String>();
        for (O currSRO : srObjectsList) {
            viewText.add(currSRO.getViewText(aViewName));
        }
        return viewText;
    }

    @Override
    public List<Map<Q,W>> getViews() {
        List<Map<Q,W>> newList = new ArrayList<Map<Q,W>>();
        for (O currSRO : srObjectsList) {
            newList.add(currSRO.getViews());
        }
        return newList;
    }

    @Override
    public List<Q> getViewKeys() {
        if (viewOrder != null) {
            return new ArrayList<Q>(viewOrder);
        } else if ((srObjectsList != null) && (!srObjectsList.isEmpty())) {
            return srObjectsList.get(0).getViewKeys();
        } else {
            return null;
        }
    }

    @Override
    public List<Map<Q, String>> getViewsText() {
        List<Map<Q, String>> newList = new ArrayList<Map<Q, String>>();
        for (O currSRO : srObjectsList) {
            newList.add(currSRO.getViewsText());
        }
        return newList;
    }

    @Override
    public List<W> getView(Q aViewName) {
        List<W> viewText = null;
        if (aViewName != null) {
            viewText = new ArrayList<W>();
            for (O currSRO : srObjectsList) {
                viewText.add(currSRO.getView(aViewName));
            }
        } else {
            log.warn("getView aViewName is null");
        }
        return viewText;
    }

    @Override
    public List<Q> getViewOrder() {
        return viewOrder;
    }

    @Override
    public void setViewOrder(List<Q> aViewOrder) {
        viewOrder = aViewOrder;
    }

    @Override
    public boolean viewsAvailable() {
        boolean viewsAvail = false;
        for (O currSRO : srObjectsList) {
            if (currSRO.viewsAvailable()) {
                viewsAvail = true;
                break;
            }
        }
        return viewsAvail;
    }
}
