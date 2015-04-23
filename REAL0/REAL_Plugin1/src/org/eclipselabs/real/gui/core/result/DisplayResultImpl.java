package org.eclipselabs.real.gui.core.result;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipselabs.real.core.searchobject.ISearchObjectConstants;
import org.eclipselabs.real.core.searchobject.ISearchObjectGroup;
import org.eclipselabs.real.core.searchresult.IKeyedSearchResult;
import org.eclipselabs.real.core.searchresult.SearchResultUtil;
import org.eclipselabs.real.core.searchresult.resultobject.ISearchResultObject;
import org.eclipselabs.real.core.searchresult.sort.IInternalSortRequest;
import org.eclipselabs.real.core.searchresult.sort.SortingApplicability;
import org.eclipselabs.real.core.util.PerformanceUtils;
import org.eclipselabs.real.gui.core.sort.SortRequestKey;
import org.eclipselabs.real.gui.core.sort.SortRequestKeyParam;
import org.eclipselabs.real.gui.core.sort.SortRequestKeyParamType;
import org.eclipselabs.real.gui.core.sort.SortRequestKeyParamUseType;
import org.eclipselabs.real.gui.core.sotree.IDisplaySO;
import org.eclipselabs.real.gui.core.util.SearchInfo;

public class DisplayResultImpl implements IDisplayResult {

    private static final Logger log = LogManager.getLogger(DisplayResultImpl.class);
    protected String soName;
    protected ISearchObjectGroup<String> soGroup;
    protected Map<String,String> soTags;
    protected List<String> textResult = new ArrayList<String>();
    protected SearchInfo searchInfo;

    public DisplayResultImpl () {}

    public <R extends IKeyedSearchResult<O>, O extends ISearchResultObject> DisplayResultImpl(R res, IDisplaySO displaySearchObject) {
        if (res != null) {
            List<IInternalSortRequest> useRequests = getApplicableSortRequests(res, displaySearchObject);
            IInternalSortRequest requestToUse = null;
            if ((useRequests != null) && (!useRequests.isEmpty())) {
                requestToUse = useRequests.get(0);
            } else if ((displaySearchObject.getSearchObject().getSortRequestList() != null) && (!displaySearchObject.getSearchObject().getSortRequestList().isEmpty())) {
                requestToUse = displaySearchObject.getSearchObject().getSortRequestList().get(0);
            } else {
                log.warn("No applicable sort requests for the search result " + displaySearchObject.getSearchObject().getSearchObjectName() + " no intelligent merge possible");
            }
            // guess years for the main result just in case
            res.guessYears();
            // sorting can be performed only with the correct SortApplicability scope
            if (requestToUse != null) {
                requestToUse.sortResultObjects(res);
            }
            init(res, displaySearchObject, res.getCachedReplaceTable());
        } else {
            log.error("Constructor null SearchResult passed");
        }
    }

    public <R extends IKeyedSearchResult<O>, O extends ISearchResultObject> DisplayResultImpl(Map<String, R> arg0, IDisplaySO displaySearchObject) {
        if ((arg0 != null) && (!arg0.isEmpty())) {
            IInternalSortRequest requestToUse = null;
            // the search results were run with the same parameters use the first one
            // to get the sort requests
            List<IInternalSortRequest> useRequests = getApplicableSortRequests(arg0.entrySet().iterator().next().getValue(), displaySearchObject);
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
            init(mainSearchRes, displaySearchObject, mainSearchRes.getCachedReplaceTable());
        } else {
            log.error("Constructor null map passed");
        }
    }

    public <R extends IKeyedSearchResult<O>, O extends ISearchResultObject> DisplayResultImpl(List<R> searchResults, IDisplaySO displaySearchObject) {
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
            init(mainSearchRes, displaySearchObject, mainSearchRes.getCachedReplaceTable());
        } else {
            log.error("Constructor null list passed");
        }
    }

    private <R extends IKeyedSearchResult<O>, O extends ISearchResultObject> void init(R searchResult, IDisplaySO displaySearchObject, Map<String,String> replaceMap) {
        if ((searchResult != null) && (searchResult.getSRObjects() != null) && (!searchResult.getSRObjects().isEmpty())) {
            setKeyValues(searchResult);
            int gcCount = 0;
            // the default GC max count if 1000
            int gcMaxCount = PerformanceUtils.getIntProperty(ISearchObjectConstants.PERF_CONST_MAX_GC_COUNT, 1000);
            for(O searchRes : searchResult.getSRObjects()) {
                addText(searchRes.getText());
                searchRes.clean();
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
            log.error("init Constructor null list passed");
        }
    }

    protected <R extends IKeyedSearchResult<O>, O extends ISearchResultObject> void setKeyValues(R searchResult) {
        if (searchResult != null) {
            soName = searchResult.getSearchObjectName();
            soGroup = searchResult.getSearchObjectGroup();
            if (searchResult.getSearchObjectTags() != null) {
                soTags = new HashMap<String, String>(searchResult.getSearchObjectTags());
            }
        } else {
            log.error("Null search result for adding key values");
        }
    }

    protected <R extends IKeyedSearchResult<O>, O extends ISearchResultObject> List<IInternalSortRequest>
                getApplicableSortRequests(R searchResult, IDisplaySO displaySearchObject) {

        List<IInternalSortRequest> newLst = null;
        if ((displaySearchObject.getSortRequestKeys() != null) && (!displaySearchObject.getSortRequestKeys().isEmpty())) {
            newLst = new ArrayList<>();
            for (final SortRequestKey currKey : displaySearchObject.getSortRequestKeys()) {
                Predicate<IInternalSortRequest> currPred = new Predicate<IInternalSortRequest>() {

                    @Override
                    public boolean test(IInternalSortRequest t) {
                        boolean sortReqMatches = true;
                        if (!t.getType().equals(currKey.getSortingType())) {
                            sortReqMatches = false;
                        }
                        if ((sortReqMatches) && (currKey.getParamList() != null) && (!currKey.getParamList().isEmpty())) {
                            for (SortRequestKeyParam<?> currParam : currKey.getParamList()) {
                                if (currParam.getUseType().equals(SortRequestKeyParamUseType.MATCH)) {
                                    if (currParam.getType().equals(SortRequestKeyParamType.SORT_APPLICABILITY)) {
                                        if (!((SortingApplicability)currParam.getValue()).equals(t.getSortApplicability())) {
                                            sortReqMatches = false;
                                        }
                                    } else if (currParam.getType().equals(SortRequestKeyParamType.NAME)) {
                                        if (!((String)currParam.getValue()).equals(t.getName())) {
                                            sortReqMatches = false;
                                        }
                                    } else {
                                        log.warn("Unknown MATCH sort request param type " + currParam.getType());
                                    }
                                }
                            }
                        }
                        return sortReqMatches;
                    }
                };
                // get copies of matching sort requests
                List<IInternalSortRequest> applicableReqs = searchResult.getSortRequests(currPred);
                if ((applicableReqs != null) && (!applicableReqs.isEmpty())) {
                    for (IInternalSortRequest applReq : applicableReqs) {
                        if ((currKey.getParamList() != null) && (!currKey.getParamList().isEmpty())) {
                            for (SortRequestKeyParam<?> currParam : currKey.getParamList()) {
                                if (currParam.getUseType().equals(SortRequestKeyParamUseType.INSTALL)) {
                                    if (currParam.getType().equals(SortRequestKeyParamType.SORT_APPLICABILITY)) {
                                        applReq.setSortApplicability((SortingApplicability)currParam.getValue());
                                    } else if (currParam.getType().equals(SortRequestKeyParamType.NAME)) {
                                        applReq.setName((String)currParam.getValue());
                                    } else {
                                        log.warn("Unknown INSTALL sort request param type " + currParam.getType());
                                    }
                                }
                            }
                        }
                    }
                    newLst.addAll(applicableReqs);
                }
            }

        }
        return newLst;
    }

    @Override
    public List<String> getText() {
        return textResult;
    }

    @Override
    public String getTextConcat() {
        StringBuilder sb = new StringBuilder();
        for (String textItem : textResult) {
            sb.append(textItem);
        }
        return sb.toString();
    }

    @Override
    public String getTextConcatWithCleanup() {
        StringBuilder sb = new StringBuilder();
        int gcCount = 0;
        Iterator<String> it = textResult.iterator();
        // the default GC max count if 1000
        int gcMaxCount = PerformanceUtils.getIntProperty(ISearchObjectConstants.PERF_CONST_MAX_GC_COUNT, 1000);
        while(it.hasNext()) {
            sb.append(it.next());
            it.remove();
            gcCount++;
            // clean up while appending to avoid consuming too much RAM
            if (gcCount > gcMaxCount) {
                System.gc();
                gcCount = 0;
            }
        }
        return sb.toString();
    }

    @Override
    public void addText(String newText) {
        textResult.add(newText);
    }

    @Override
    public String getSearchObjectName() {
        return soName;
    }

    @Override
    public void setSearchObjectName(String newName) {
        soName = newName;
    }

    @Override
    public ISearchObjectGroup<String> getSearchObjectGroup() {
        return soGroup;
    }

    @Override
    public void setSearchObjectGroup(ISearchObjectGroup<String> newGroup) {
        soGroup = newGroup;
    }

    @Override
    public Map<String, String> getSearchObjectTags() {
        return soTags;
    }

    @Override
    public void setSearchObjectTags(Map<String, String> tagsMap) {
        soTags = new HashMap<String, String>(tagsMap);
    }

    @Override
    public Long getTextConcatLength() {
        long concatLength = 0;
        for (String textItem : textResult) {
            concatLength += textItem.length();
        }
        return concatLength;
    }

    @Override
    public SearchInfo getSearchInfo() {
        return searchInfo;
    }

    @Override
    public void setSearchInfo(SearchInfo newInfo) {
        searchInfo = newInfo;
    }

}
