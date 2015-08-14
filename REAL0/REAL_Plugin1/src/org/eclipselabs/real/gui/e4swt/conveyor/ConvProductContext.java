package org.eclipselabs.real.gui.e4swt.conveyor;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipselabs.real.core.searchobject.PerformSearchRequest;
import org.eclipselabs.real.core.searchobject.param.IReplaceableParam;
import org.eclipselabs.real.core.searchobject.param.ReplaceableParamKey;
import org.eclipselabs.real.core.searchresult.IKeyedComplexSearchResult;
import org.eclipselabs.real.core.searchresult.ISearchResult;
import org.eclipselabs.real.core.searchresult.resultobject.IComplexSearchResultObject;
import org.eclipselabs.real.core.searchresult.resultobject.ISearchResultObject;
import org.eclipselabs.real.core.util.NamedThreadFactory;
import org.eclipselabs.real.gui.core.util.SearchInfo;
import org.eclipselabs.real.gui.e4swt.parts.GUISearchResult;

public class ConvProductContext {

    protected ExecutorService asyncExecutorService;

    protected String searchID;

    protected SearchInfo searchInfo;

    protected Map<ReplaceableParamKey, IReplaceableParam<?>> currentParamMap;

    protected volatile boolean proceed = true;

    protected volatile boolean complete = false;

    protected String abortMessage;

    protected MPart searchPart;

    protected GUISearchResult guiObjectRef;

    protected volatile PerformSearchRequest searchRequest;

    protected Map<String,IKeyedComplexSearchResult<? extends IComplexSearchResultObject<
                    ? extends ISearchResult<? extends ISearchResultObject>,? extends ISearchResultObject,String>,
                ? extends ISearchResult<? extends ISearchResultObject>,
                ? extends ISearchResultObject,
            String>> result;

    protected Map<String, Object> customParams = new ConcurrentHashMap<String, Object>();

    public ConvProductContext(String productName) {
        String finalProductName = (productName == null)?"":productName;
        asyncExecutorService = Executors.newCachedThreadPool(new NamedThreadFactory(finalProductName));
    }

    public ConvProductContext(String productName, int asyncThreads) {
        String finalProductName = (productName == null)?"":productName;
        asyncExecutorService = Executors.newFixedThreadPool(asyncThreads, new NamedThreadFactory(finalProductName));
    }

    /**
     * This method is called after the context is "completed"
     * that is the conveyor has been executed and is being returned
     * as a result of the operation
     */
    public void cleanup() {
        // orderly shutdown as no tasks should be working
        asyncExecutorService.shutdown();
    }

    public synchronized ExecutorService getAsyncExecutorService() {
        return asyncExecutorService;
    }

    public synchronized void setAsyncExecutorService(ExecutorService asyncExecutorService) {
        this.asyncExecutorService = asyncExecutorService;
    }

    public String getSearchID() {
        return searchID;
    }

    public void setSearchID(String searchID) {
        this.searchID = searchID;
    }

    public SearchInfo getSearchInfo() {
        return searchInfo;
    }

    public void setSearchInfo(SearchInfo searchInfo) {
        this.searchInfo = searchInfo;
    }

    public Map<ReplaceableParamKey, IReplaceableParam<?>> getCurrentParamMap() {
        return currentParamMap;
    }

    public void setCurrentParamMap(Map<ReplaceableParamKey, IReplaceableParam<?>> currentParamMap) {
        this.currentParamMap = currentParamMap;
    }

    public synchronized boolean isProceed() {
        return proceed;
    }

    public synchronized void setProceed(boolean proceed) {
        this.proceed = proceed;
    }

    public synchronized boolean isComplete() {
        return complete;
    }

    public synchronized void setComplete(boolean complete) {
        this.complete = complete;
    }

    public String getAbortMessage() {
        return abortMessage;
    }

    public void setAbortMessage(String abortMessage) {
        this.abortMessage = abortMessage;
    }

    public MPart getSearchPart() {
        return searchPart;
    }

    public void setSearchPart(MPart searchPart) {
        this.searchPart = searchPart;
    }

    public synchronized GUISearchResult getGuiObjectRef() {
        return guiObjectRef;
    }

    public synchronized void setGuiObjectRef(GUISearchResult guiObjectRef) {
        this.guiObjectRef = guiObjectRef;
    }

    public synchronized PerformSearchRequest getSearchRequest() {
        return searchRequest;
    }

    public synchronized void setSearchRequest(PerformSearchRequest searchRequest) {
        this.searchRequest = searchRequest;
    }

    public Map<String, IKeyedComplexSearchResult<? extends
            IComplexSearchResultObject<? extends ISearchResult<? extends ISearchResultObject>,
                    ? extends ISearchResultObject, String>,
                ? extends ISearchResult<? extends ISearchResultObject>,
                ? extends ISearchResultObject, String>>
            getResult() {
        return result;
    }

    public void setResult(Map<String, IKeyedComplexSearchResult<? extends
                    IComplexSearchResultObject<? extends ISearchResult<? extends ISearchResultObject>,
                            ? extends ISearchResultObject, String>,
                    ? extends ISearchResult<? extends ISearchResultObject>,
                    ? extends ISearchResultObject, String>> result) {
        this.result = result;
    }

    public Object getParam(String key) {
        return customParams.get(key);
    }

    public void setParam(String key, Object obj) {
        customParams.put(key, obj);
    }

}
