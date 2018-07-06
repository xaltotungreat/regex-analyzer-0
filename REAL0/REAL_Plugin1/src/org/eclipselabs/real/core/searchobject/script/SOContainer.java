package org.eclipselabs.real.core.searchobject.script;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipselabs.real.core.distrib.GenericError;
import org.eclipselabs.real.core.distrib.IDistribRoot;
import org.eclipselabs.real.core.distrib.IDistribTaskResultWrapper;
import org.eclipselabs.real.core.dlog.DAccumulatorSearchResult;
import org.eclipselabs.real.core.dlog.DBuilderSearch;
import org.eclipselabs.real.core.exception.IncorrectPatternException;
import org.eclipselabs.real.core.logfile.ILogFileAggregate;
import org.eclipselabs.real.core.logfile.ILogFileAggregateRead;
import org.eclipselabs.real.core.logfile.LogFileController;
import org.eclipselabs.real.core.searchobject.IKeyedComplexSearchObject;
import org.eclipselabs.real.core.searchobject.ISOComplexRegex;
import org.eclipselabs.real.core.searchobject.ISOComplexRegexView;
import org.eclipselabs.real.core.searchobject.PerformSearchRequest;
import org.eclipselabs.real.core.searchresult.IKeyedComplexSearchResult;
import org.eclipselabs.real.core.searchresult.ISRComplexRegexView;
import org.eclipselabs.real.core.searchresult.ISRSearchScript;
import org.eclipselabs.real.core.searchresult.SearchResultUtil;
import org.eclipselabs.real.core.searchresult.resultobject.IComplexSearchResultObject;
import org.eclipselabs.real.core.searchresult.resultobject.ISROComplexRegexView;
import org.eclipselabs.real.core.searchresult.sort.IInternalSortRequest;
import org.eclipselabs.real.core.util.PerformanceUtils;

/**
 * This is the container for a search object. This container is necessary for 2 reasons:
 * - the search object itself has a lot of generic parameters, these parameters are unwieldy to use in scripts
 * - the container provides additional API to work with search object parameters.
 *
 * @author Vadim Korkin
 *
 */
public class SOContainer {

    private static final Logger log = LogManager.getLogger(SOContainer.class);
    protected volatile IKeyedComplexSearchObject<? extends IKeyedComplexSearchResult<? extends IComplexSearchResultObject<ISRComplexRegexView, ISROComplexRegexView, String>,
            ISRComplexRegexView, ISROComplexRegexView, String>,
            ? extends IComplexSearchResultObject<ISRComplexRegexView, ISROComplexRegexView, String>,
            ISOComplexRegexView, ISRComplexRegexView, ISROComplexRegexView, String> searchObject;

    protected volatile ISRSearchScript scriptResult;
    protected volatile String logText;

    public SOContainer(ISOComplexRegex containerSO, ISRSearchScript scrRes) {
        searchObject = containerSO;
        scriptResult = scrRes;
    }

    public SOContainer(ISOComplexRegex containerSO, ISRSearchScript scrRes, String text) {
        this(containerSO, scrRes);
        logText = text;
    }

    /**
     * Is used in scripts to verify this object is not null. In case a returned container contains no search object.
     * @return true if the search object in this container is null
     */
    public boolean isNull() {
        return searchObject == null;
    }

    /**
     * Executes the search in the log files specified by the search object. For ease of use the result of this method
     * is a {@link SRContainer} object. This may be a search in current or a search in the log files.
     * @return the {@link SRContainer} object that contains the result of the search.
     */
    public <R extends IKeyedComplexSearchResult<O, ISRComplexRegexView, ISROComplexRegexView, String>,
                O extends IComplexSearchResultObject<ISRComplexRegexView, ISROComplexRegexView, String>> SRContainer execute() throws IncorrectPatternException {
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
            if (!LogFileController.INSTANCE.isLogFilesAvailable(searchObject.getRequiredLogTypes())) {
                log.error("execute SO no log files available for this SO returning empty container");
                resultContainer = new SRContainer(null, scriptResult);
                return resultContainer;
            }
            // before a new search reset the completed SO Files
            // total SO Files will be set in submitSearch
            scriptResult.getProgressMonitor().resetCompletedSOFiles();
            PerformSearchRequest req = new PerformSearchRequest(null, scriptResult.getProgressMonitor(), scriptResult.getCachedReplaceParams(),
                    scriptResult.getCachedReplaceTable(), scriptResult.getRegexFlags());
            ILogFileAggregateRead logAggr = LogFileController.INSTANCE.getLogAggregate(paramSO.getLogFileType());
            // fill in the total number of files
            req.getProgressMonitor().setTotalSOFiles(logAggr.getCount());
            // create a distribution system for this search
            DBuilderSearch<R> dBuilder = new DBuilderSearch<>(logAggr, paramSO, req);
            int threadsNumber = PerformanceUtils.getIntProperty(ILogFileAggregate.PERF_CONST_SEARCH_THREADS, 2);
            final IDistribRoot<R, DAccumulatorSearchResult<R>, List<IDistribTaskResultWrapper<R>>, GenericError> distribRoot = dBuilder.build(threadsNumber);

             CompletableFuture<? extends Map<String, R>> future = distribRoot.execute().handleAsync((DAccumulatorSearchResult<R> accumResult, Throwable t) -> {
                Map<String, R> oldMapResults = null;
                if ((accumResult != null) && (!accumResult.getResult().isEmpty())) {
                    /* for now convert the value to the old Map format.
                     * If this is successful the old format will be replaced
                     */
                    try {
                        distribRoot.close();
                    } catch (Exception e) {
                        log.error("Exception shutting down distribution root " + distribRoot, e);
                    }
                    AtomicInteger tempCounter = new AtomicInteger(0);
                    String initValue = "oldFormat";
                    oldMapResults = accumResult.getResult().stream().filter(tr -> tr.getActualResult() != null).collect(Collectors.toMap(
                            (IDistribTaskResultWrapper<R> forKey) -> initValue + tempCounter.incrementAndGet(),
                            (IDistribTaskResultWrapper<R> forValue) -> forValue.getActualResult()));

                }
                return oldMapResults;
            });

            if (future == null) {
                log.warn("execute search no log files null result");
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
            // if logText is not null it is a search in the current text
            // it means only one file for every search.
            // total SO Files are set to 1 in the search task no need to reset
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
