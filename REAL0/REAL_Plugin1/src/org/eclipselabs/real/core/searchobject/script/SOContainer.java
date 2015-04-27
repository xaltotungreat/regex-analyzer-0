package org.eclipselabs.real.core.searchobject.script;

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipselabs.real.core.logfile.LogFileControllerImpl;
import org.eclipselabs.real.core.searchobject.IKeyedComplexSearchObject;
import org.eclipselabs.real.core.searchobject.ISOComplexRegexView;
import org.eclipselabs.real.core.searchobject.PerformSearchRequest;
import org.eclipselabs.real.core.searchresult.IKeyedComplexSearchResult;
import org.eclipselabs.real.core.searchresult.ISRComplexRegexView;
import org.eclipselabs.real.core.searchresult.ISRSearchScript;
import org.eclipselabs.real.core.searchresult.SearchResultUtil;
import org.eclipselabs.real.core.searchresult.resultobject.IComplexSearchResultObject;
import org.eclipselabs.real.core.searchresult.resultobject.ISROComplexRegexView;
import org.eclipselabs.real.core.searchresult.sort.IInternalSortRequest;

public class SOContainer {

    private static final Logger log = LogManager.getLogger(SOContainer.class);
    protected volatile IKeyedComplexSearchObject<? extends IKeyedComplexSearchResult<? extends IComplexSearchResultObject<ISRComplexRegexView, ISROComplexRegexView, String>,
            ISRComplexRegexView, ISROComplexRegexView, String>,
            ? extends IComplexSearchResultObject<ISRComplexRegexView, ISROComplexRegexView, String>,
            ISOComplexRegexView, ISRComplexRegexView, ISROComplexRegexView, String> searchObject;

    protected volatile ISRSearchScript scriptResult;
    protected volatile String logText;

    public SOContainer(IKeyedComplexSearchObject<? extends IKeyedComplexSearchResult<? extends IComplexSearchResultObject<ISRComplexRegexView, ISROComplexRegexView, String>,
            ISRComplexRegexView, ISROComplexRegexView, String>,
            ? extends IComplexSearchResultObject<ISRComplexRegexView, ISROComplexRegexView, String>,
            ISOComplexRegexView, ISRComplexRegexView, ISROComplexRegexView, String> containerSO, ISRSearchScript scrRes) {
        searchObject = containerSO;
        scriptResult = scrRes;
    }

    public SOContainer(IKeyedComplexSearchObject<? extends IKeyedComplexSearchResult<? extends IComplexSearchResultObject<ISRComplexRegexView, ISROComplexRegexView, String>,
            ISRComplexRegexView, ISROComplexRegexView, String>,
            ? extends IComplexSearchResultObject<ISRComplexRegexView, ISROComplexRegexView, String>,
            ISOComplexRegexView, ISRComplexRegexView, ISROComplexRegexView, String> containerSO, ISRSearchScript scrRes, String text) {
        this(containerSO, scrRes);
        logText = text;
    }

    public boolean isNull() {
        return searchObject == null;
    }

    public <R extends IKeyedComplexSearchResult<O, ISRComplexRegexView, ISROComplexRegexView, String>,
                O extends IComplexSearchResultObject<ISRComplexRegexView, ISROComplexRegexView, String>> SRContainer execute() {
        SRContainer resultContainer = null;
        if (searchObject == null) {
            log.error("execute SO is null returning empty container");
            resultContainer = new SRContainer(null, scriptResult);
            return resultContainer;
        }
        log.debug("Executing " + searchObject.getSearchObjectName());
        IKeyedComplexSearchObject<R,O,ISOComplexRegexView, ISRComplexRegexView, ISROComplexRegexView, String> paramSO
            = (IKeyedComplexSearchObject<R,O,ISOComplexRegexView, ISRComplexRegexView, ISROComplexRegexView, String>)searchObject;

        if (logText == null) {
            if (!LogFileControllerImpl.INSTANCE.isLogFilesAvailable(searchObject.getRequiredLogTypes())) {
                log.error("execute SO no log files available for this SO returning empty container");
                resultContainer = new SRContainer(null, scriptResult);
                return resultContainer;
            }
            // before a new search reset the completed SO Files
            // total SO Files will be set in submitSearch
            scriptResult.getProgressMonitor().resetCompletedSOFiles();
            PerformSearchRequest req = new PerformSearchRequest(null, scriptResult.getProgressMonitor(), scriptResult.getCachedReplaceParams(),
                    scriptResult.getCachedReplaceTable(), scriptResult.getRegexFlags());
            final CompletableFuture<? extends Map<String, R>> future =
                        LogFileControllerImpl.INSTANCE.getLogAggregate(searchObject.getLogFileType()).submitSearch(paramSO, req);
            if (future == null) {
                log.warn("execute search nu log files null result");
                resultContainer = new SRContainer(null, scriptResult);
            } else {
                try {
                    Map<String, R> mapResults = future.get();
                    if ((mapResults != null) && (!mapResults.isEmpty())) {
                        IInternalSortRequest requestToUse = null;
                        if ((searchObject.getSortRequestList() != null) && (!searchObject.getSortRequestList().isEmpty())) {
                            requestToUse = searchObject.getSortRequestList().get(0);
                        } else {
                            log.warn("No applicable sort requests for the search result " + searchObject.getSearchObjectName() + " no intelligent merge possible");
                        }
                        // intelligent merging can be performed only with the correct SortApplicability scope
                        R mainSearchRes = SearchResultUtil.merge(new ArrayList<>(mapResults.values()), requestToUse);
                        // sorting can be performed only with the correct SortApplicability scope
                        mainSearchRes.sort(requestToUse);
                        resultContainer = new SRContainer(mainSearchRes, scriptResult);
                    } else {
                        log.warn("execute search in log files no results returned");
                        resultContainer = new SRContainer(null, scriptResult);
                    }
                } catch (InterruptedException | ExecutionException e) {
                    log.error("execute ",e);
                }
            }
        } else {
            // if logText is not null it is a search in current
            // it means only one file for every search.
            // total So Files are set to 1 in the search task no need to reset
            // before a new search reset the completed SO Files
            scriptResult.getProgressMonitor().resetCompletedSOFiles();
            PerformSearchRequest req = new PerformSearchRequest(logText, scriptResult.getProgressMonitor(), scriptResult.getCachedReplaceParams(),
                    scriptResult.getCachedReplaceTable(), scriptResult.getRegexFlags());
            R searchResult = paramSO.performSearch(req);
            scriptResult.getProgressMonitor().incrementCompletedSOFiles();
            req.setText(null);
            if ((searchResult != null) && (searchResult.getSRObjects() != null) && (!searchResult.getSRObjects().isEmpty())) {
                resultContainer = new SRContainer(searchResult, scriptResult);
                log.debug("SOName=" + paramSO.getSearchObjectName() + " results number=" + searchResult.getSRObjects().size());
            } else {
                log.warn("execute search in log text no results returned");
                resultContainer = new SRContainer(null, scriptResult);
            }
        }
        return resultContainer;
    }


}
