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

    protected volatile ExecutorService asyncExecutorService;

    protected volatile String searchID;

    protected volatile SearchInfo searchInfo;

    protected volatile Map<ReplaceableParamKey, IReplaceableParam<?>> currentParamMap;

    protected volatile boolean proceed = true;

    protected volatile boolean complete = false;

    protected volatile String abortMessage;

    protected volatile MPart searchPart;

    protected volatile GUISearchResult guiObjectRef;

    protected volatile PerformSearchRequest searchRequest;

    protected volatile Map<String,IKeyedComplexSearchResult<? extends IComplexSearchResultObject<
                    ? extends ISearchResult<? extends ISearchResultObject>,? extends ISearchResultObject,String>,
                ? extends ISearchResult<? extends ISearchResultObject>,
                ? extends ISearchResultObject,
            String>> result;

    protected volatile Map<String, Object> customParams = new ConcurrentHashMap<>();

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
    public synchronized void cleanup() {
        // orderly shutdown as no tasks should be working
        asyncExecutorService.shutdown();
    }

    public synchronized ExecutorService getAsyncExecutorService() {
        return asyncExecutorService;
    }

    public synchronized void setAsyncExecutorService(ExecutorService asyncExecutorService) {
        this.asyncExecutorService = asyncExecutorService;
    }

    public synchronized String getSearchID() {
        return searchID;
    }

    public synchronized void setSearchID(String searchID) {
        this.searchID = searchID;
    }

    public synchronized SearchInfo getSearchInfo() {
        return searchInfo;
    }

    public synchronized void setSearchInfo(SearchInfo searchInfo) {
        this.searchInfo = searchInfo;
    }

    public synchronized Map<ReplaceableParamKey, IReplaceableParam<?>> getCurrentParamMap() {
        return currentParamMap;
    }

    public synchronized void setCurrentParamMap(Map<ReplaceableParamKey, IReplaceableParam<?>> currentParamMap) {
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

    public synchronized String getAbortMessage() {
        return abortMessage;
    }

    public synchronized void setAbortMessage(String abortMessage) {
        this.abortMessage = abortMessage;
    }

    public synchronized MPart getSearchPart() {
        return searchPart;
    }

    public synchronized void setSearchPart(MPart searchPart) {
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

    public synchronized Map<String, IKeyedComplexSearchResult<? extends
            IComplexSearchResultObject<? extends ISearchResult<? extends ISearchResultObject>,
                    ? extends ISearchResultObject, String>,
                ? extends ISearchResult<? extends ISearchResultObject>,
                ? extends ISearchResultObject, String>>
            getResult() {
        return result;
    }

    public synchronized void setResult(Map<String, IKeyedComplexSearchResult<? extends
                    IComplexSearchResultObject<? extends ISearchResult<? extends ISearchResultObject>,
                            ? extends ISearchResultObject, String>,
                    ? extends ISearchResult<? extends ISearchResultObject>,
                    ? extends ISearchResultObject, String>> result) {
        this.result = result;
    }

    public synchronized Object getParam(String key) {
        return customParams.get(key);
    }

    public synchronized void setParam(String key, Object obj) {
        customParams.put(key, obj);
    }

}
