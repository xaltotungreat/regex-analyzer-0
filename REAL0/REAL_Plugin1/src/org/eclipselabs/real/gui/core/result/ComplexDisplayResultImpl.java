package org.eclipselabs.real.gui.core.result;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipselabs.real.core.searchobject.ISearchObjectConstants;
import org.eclipselabs.real.core.searchresult.IKeyedComplexSearchResult;
import org.eclipselabs.real.core.searchresult.ISearchResult;
import org.eclipselabs.real.core.searchresult.SearchResultUtil;
import org.eclipselabs.real.core.searchresult.resultobject.IComplexSearchResultObject;
import org.eclipselabs.real.core.searchresult.resultobject.ISearchResultObject;
import org.eclipselabs.real.core.searchresult.sort.IInternalSortRequest;
import org.eclipselabs.real.core.util.PerformanceUtils;
import org.eclipselabs.real.gui.core.sotree.IDisplaySO;

public class ComplexDisplayResultImpl extends DisplayResultImpl implements IComplexDisplayResult {

    private static final Logger log = LogManager.getLogger(ComplexDisplayResultImpl.class);
    protected List<IDRViewItem> items = new ArrayList<IDRViewItem>();
    protected List<String> viewNames = new ArrayList<String>();

    /**
     * For one result it is usually a search in current. For this type of search
     * the start and end positions are not recalculated. They remain the same because they
     * must be the position within a piece of text in the existing result.
     * @param searchResult the result
     * @param displaySearchObject the DSO for this result
     * @param replaceMap to replace parameters
     */
    public <R extends IKeyedComplexSearchResult<O, W, X, String>,
                O extends IComplexSearchResultObject<W, X, String>,
                W extends ISearchResult<X>, X extends ISearchResultObject>
            ComplexDisplayResultImpl(R searchResult, IDisplaySO displaySearchObject) {
        if (searchResult != null) {
            IInternalSortRequest requestToUse = null;
            List<IInternalSortRequest> useRequests = getApplicableSortRequests(searchResult, displaySearchObject);
            if ((useRequests != null) && (!useRequests.isEmpty())) {
                requestToUse = useRequests.get(0);
            } else if ((displaySearchObject.getSearchObject().getSortRequestList() != null) && (!displaySearchObject.getSearchObject().getSortRequestList().isEmpty())) {
                requestToUse = displaySearchObject.getSearchObject().getSortRequestList().get(0);
            } else {
                log.warn("No applicable sort requests for the search result " + displaySearchObject.getSearchObject().getSearchObjectName() + " no intelligent merge possible");
            }
            // guess years for the main result just in case
            searchResult.guessYears();
            // sorting can be performed only with the correct SortApplicability scope
            if (requestToUse != null) {
                requestToUse.sortResultObjects(searchResult);
            }
            initLeavePos(searchResult, displaySearchObject);
        } else {
            log.error("Null result");
        }
    }

    private <R extends IKeyedComplexSearchResult<O, W, X, String>,
                        O extends IComplexSearchResultObject<W, X, String>,
                        W extends ISearchResult<X>, X extends ISearchResultObject>
            void initLeavePos(R searchResult, IDisplaySO displaySearchObject) {
        setKeyValues(searchResult);
        for (String viewName : searchResult.getViewKeys()) {
            for (String viewPattern : displaySearchObject.getViewNamePatterns()) {
                Pattern pt = Pattern.compile(viewPattern);
                Matcher mt = pt.matcher(viewName);
                if (mt.matches()) {
                    addViewName(viewName);
                    log.debug("Adding view for search result " + viewName);
                }
            }
        }
        // the default GC max count if 1000
        int gcMaxCount = PerformanceUtils.getIntProperty(ISearchObjectConstants.PERF_CONST_MAX_GC_COUNT, 1000);
        if ((searchResult.getSRObjects() != null) && (!searchResult.getSRObjects().isEmpty())) {
            int gcCount = 0;
            for (IComplexSearchResultObject<W, X, String> srO : searchResult.getSRObjects()) {
                String currText = null;
                if ((displaySearchObject.getTextViewName() != null)
                        && (!("".equals(displaySearchObject.getTextViewName())))) {
                    currText = srO.getViewText(displaySearchObject.getTextViewName());
                } else {
                    currText = srO.getText();
                }
                IDRViewItem complResultItem = new DRViewItemImpl(srO.getStartPos(), srO.getEndPos());
                for (String viewName : getViewNames()) {
                    if (getViewNames().contains(viewName)) {
                        complResultItem.addViewText(srO.getViewText(viewName));
                    }
                }
                if (srO.getDate() != null) {
                    complResultItem.setDate(srO.getDate());
                }

                addText(currText);
                addViewItem(complResultItem);
                gcCount++;
                // for a complex regex it may be also important to collect garbage manually
                // If the search request contains too many records and the garbage is not collected manually
                // memory spikes may occur
                // the problem with these spikes is that once the heap reaches a certain value
                // other applications are unloaded from RAM. GC1 will contract the heap but other applications
                // will be slower
                if (gcCount > gcMaxCount) {
                    System.gc();
                    gcCount = 0;
                }
            }
        } else {
            log.error("initLeavePos No result objects");
        }
    }

    /**
     *
     * @param searchResults
     * @param displaySearchObject
     * @param replaceMap
     */
    public <R extends IKeyedComplexSearchResult<O, W, X, String>,
                    O extends IComplexSearchResultObject<W, X, String>,
                    W extends ISearchResult<X>, X extends ISearchResultObject>
            ComplexDisplayResultImpl(List<R> searchResults, IDisplaySO displaySearchObject) {
        if ((searchResults != null) && (!searchResults.isEmpty())) {
            R tmpRes = searchResults.get(0);
            List<IInternalSortRequest> useRequests = getApplicableSortRequests(tmpRes, displaySearchObject);
            IInternalSortRequest requestToUse = null;
            if ((useRequests != null) && (!useRequests.isEmpty())) {
                requestToUse = useRequests.get(0);
            } else if ((displaySearchObject.getSearchObject().getSortRequestList() != null) && (!displaySearchObject.getSearchObject().getSortRequestList().isEmpty())) {
                requestToUse = displaySearchObject.getSearchObject().getSortRequestList().get(0);
            } else {
                log.warn("No applicable sort requests for the search result " + displaySearchObject.getSearchObject().getSearchObjectName() + " no intelligent merge possible");
            }
            // intelligent merging can be performed only with the correct SortApplicability scope
            // just in case add all results to a new ArrayList (to allow for unmodifiable lists)
            R mainSearchRes = SearchResultUtil.merge(new ArrayList<>(searchResults), requestToUse);
            // sorting can be performed only with the correct SortApplicability scope
            if (requestToUse != null) {
                requestToUse.sortResultObjects(mainSearchRes);
            }
            //mainSearchRes.sort(requestToUse);
            initRecalculatePos(mainSearchRes, displaySearchObject, mainSearchRes.getCachedReplaceTable());
        } else {
            log.error("Null size results list");
        }
    }

    /**
     * If the result is a map with files and results it must be an initial search.
     * Here the positions are recalculated to correspond to the text in the result.
     * @param arg0 map with FilePath,SearchResult
     * @param displaySearchObject the DSo for the result
     * @param replaceMap to replace parameters
     */
    public <R extends IKeyedComplexSearchResult<O, W, X, String>,
                    O extends IComplexSearchResultObject<W, X, String>,
                    W extends ISearchResult<X>, X extends ISearchResultObject>
            ComplexDisplayResultImpl(Map<String, R> arg0, IDisplaySO displaySearchObject) {

        if ((arg0 != null) && (!arg0.isEmpty())) {
            // the search results were run with the same parameters use the first one
            // to get the sort requests
            List<IInternalSortRequest> useRequests = getApplicableSortRequests(arg0.entrySet().iterator().next().getValue(), displaySearchObject);
            IInternalSortRequest requestToUse = null;
            if ((useRequests != null) && (!useRequests.isEmpty())) {
                requestToUse = useRequests.get(0);
            } else if ((displaySearchObject.getSearchObject().getSortRequestList() != null) && (!displaySearchObject.getSearchObject().getSortRequestList().isEmpty())) {
                requestToUse = displaySearchObject.getSearchObject().getSortRequestList().get(0);
            } else {
                log.warn("No applicable sort requests for the search result " + displaySearchObject.getSearchObject().getSearchObjectName() + " no intelligent merge possible");
            }
            // intelligent merging can be performed only with the correct SortApplicability scope
            R mainSearchRes = SearchResultUtil.merge(new ArrayList<>(arg0.values()), requestToUse);
            // sorting can be performed only with the correct SortApplicability scope
            if (requestToUse != null) {
                requestToUse.sortResultObjects(mainSearchRes);
            }
            //mainSearchRes.sort(requestToUse);
            initRecalculatePos(mainSearchRes, displaySearchObject, mainSearchRes.getCachedReplaceTable());
        } else {
            log.error("Null size results list");
        }
    }

    private <R extends IKeyedComplexSearchResult<O, W, X, String>,
                    O extends IComplexSearchResultObject<W, X, String>,
                    W extends ISearchResult<X>, X extends ISearchResultObject>
            void initRecalculatePos(R searchResult, IDisplaySO displaySearchObject, Map<String, String> replaceMap) {
        if ((searchResult != null) && (searchResult.getSRObjects() != null) && (!searchResult.getSRObjects().isEmpty())) {
            setKeyValues(searchResult);
            for (String viewName : searchResult.getViewKeys()) {
                for (String viewPattern : displaySearchObject.getViewNamePatterns()) {
                    Pattern pt = Pattern.compile(viewPattern);
                    Matcher mt = pt.matcher(viewName);
                    if (mt.matches()) {
                        addViewName(viewName);
                        log.debug("Adding view for search result " + viewName);
                        break;
                    }
                }
            }
            int startPos = 0;
            int endPos = 0;
            int gcCount = 0;
            // the default GC max count if 1000
            int gcMaxCount = PerformanceUtils.getIntProperty(ISearchObjectConstants.PERF_CONST_MAX_GC_COUNT, 1000);
            for (IComplexSearchResultObject<W, X, String> srO : searchResult.getSRObjects()) {
                String currText = null;
                if ((displaySearchObject.getTextViewName() != null)
                        && (!("".equals(displaySearchObject.getTextViewName())))) {
                    currText = srO.getViewText(displaySearchObject.getTextViewName());
                } else {
                    currText = srO.getText();
                }
                // log.debug("Display Result adding text " + currText);
                startPos = endPos;
                endPos = startPos + currText.length();
                IDRViewItem complexResultItem = new DRViewItemImpl(startPos, endPos);
                for (String currViewName : getViewNames()) {
                    complexResultItem.addViewText(srO.getViewText(currViewName));
                }
                if (srO.getDate() != null) {
                    complexResultItem.setDate(srO.getDate());
                }
                addText(currText);
                addViewItem(complexResultItem);
                srO.clean();
                gcCount++;
                // for a complex regex it may be also important to collect garbage manually
                // If the search request contains too many records and the garbage is not collected manually
                // memory spikes may occur
                // the problem with these spikes is that once the heap reaches a certain value
                // other applications are unloaded from RAM. GC1 will contract the heap but other applications
                // will be slower
                if (gcCount > gcMaxCount) {
                    System.gc();
                    gcCount = 0;
                }
            }

        } else {
            log.error("initRecalculatePos Null size results list");
        }
    }

    @Override
    public List<IDRViewItem> getViewItems() {
        return items;
    }

    @Override
    public void addViewItem(IDRViewItem newItem) {
        items.add(newItem);
    }

    @Override
    public void addViewItems(Collection<IDRViewItem> newItems) {
        items.addAll(newItems);
    }

    @Override
    public List<String> getViewNames() {
        return viewNames;
    }

    @Override
    public void setViewNames(List<String> newViewNames) {
        viewNames = newViewNames;
    }

    @Override
    public void addViewName(String aName) {
        viewNames.add(aName);
    }

}
