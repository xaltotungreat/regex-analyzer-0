package org.eclipselabs.real.core.searchresult.resultobject;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipselabs.real.core.searchresult.ISearchResult;

public abstract class ComplexSearchResultObjectImpl<W extends ISearchResult<X>, X extends ISearchResultObject,Q> extends SearchResultObjectImpl
        implements IComplexSearchResultObject<W, X, Q> {
    private static final Logger log = LogManager.getLogger(ComplexSearchResultObjectImpl.class);
    protected Map<Q,W> viewMap = new ConcurrentHashMap<Q,W>();

    public ComplexSearchResultObjectImpl(String aText, Integer aStartPos, Integer aEndPos) {
        super(aText, aStartPos, aEndPos);
    }

    public ComplexSearchResultObjectImpl(String aText, Integer aStartPos, Integer aEndPos, LocalDateTime dt) {
        super(aText, aStartPos, aEndPos, dt);
    }

    @Override
    public void setViews(Map<Q, W> newMap) {
        viewMap = newMap;
    }

    @Override
    public void addView(Q aViewName, W aViewObj) {
        viewMap.put(aViewName, aViewObj);
    }

    @Override
    public Map<Q, W> getViews() {
        return viewMap;
    }

    @Override
    public W getView(Q aViewName) {
        return viewMap.get(aViewName);
    }

    @Override
    public Map<Q, String> getViewsText() {
        /*Map<Q,String> resMap = new HashMap<Q,String>();
        StringBuilder sb;
        for (Map.Entry<Q, W> entry : viewMap.entrySet()) {
            sb = new StringBuilder();
            List<String> viewContent = viewMap.get(key).getTextList();
            for (String viewItem : viewContent) {
                sb.append(viewItem);
            }
            resMap.put(key, sb.toString());
        }*/
        // the commented out code did the same thing
        // left it here in case something goes wrong
        Map<Q,String> resMap = viewMap.entrySet().stream().collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue().getTextConcat()));
        return resMap;
    }

    @Override
    public String getViewText(Q aViewName) {
        StringBuilder sb = new StringBuilder();
        if ((aViewName != null) && (viewMap.containsKey(aViewName))) {
            List<String> viewContent = viewMap.get(aViewName).getTextList();
            if (viewContent != null) {
                for (String viewItem : viewContent) {
                    sb.append(viewItem);
                }
            }
        }
        return sb.toString();
    }

    @Override
    public List<String> getViewTextList(Q aViewName) {
        if ((aViewName != null) && (viewMap.containsKey(aViewName))) {
            return viewMap.get(aViewName).getTextList();
        }
        return null;
    }

    @Override
    public Map<Q,List<String>> getViewsTextList() {
        Map<Q,List<String>> resMap = new HashMap<Q,List<String>>();
        /*for (Q key : viewMap.keySet()) {
            resMap.put(key, viewMap.get(key).getTextList());
        }*/
        for (Map.Entry<Q, W> entry : viewMap.entrySet()) {

        }
        return resMap;
    }

    @Override
    public List<Q> getViewKeys() {
        return new ArrayList<Q>(viewMap.keySet());
    }

    @Override
    public void removeView(Q aViewName) {
        if (aViewName != null) {
            viewMap.remove(aViewName);
        } else {
            log.warn("removeView aViewName is null");
        }
    }

    @Override
    public void removeViews(List<Q> aViewNames) {
        if (aViewNames != null) {
            for (Q key : aViewNames) {
                removeView(key);
            }
        } else {
            log.warn("removeViews aViewNames is null");
        }
    }

    @Override
    public boolean viewsAvailable() {
        return !viewMap.isEmpty();
    }

    @Override
    public void clean() {
        super.clean();
        if ((viewMap != null) && (!viewMap.isEmpty())) {
            viewMap.clear();
        }
    }

    @Override
    public ISearchResultObject clone() throws CloneNotSupportedException {
        ComplexSearchResultObjectImpl<W, X, Q> cloneObj = (ComplexSearchResultObjectImpl<W, X, Q>)super.clone();
        Map<Q,W> viewMap2 = new ConcurrentHashMap<Q,W>();
        if (viewMap != null) {
            viewMap2.putAll(viewMap);
        }
        cloneObj.setViews(viewMap2);
        return cloneObj;
    }


}
