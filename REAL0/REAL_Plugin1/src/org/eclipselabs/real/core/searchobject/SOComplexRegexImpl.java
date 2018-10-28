package org.eclipselabs.real.core.searchobject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipselabs.real.core.exception.IncorrectPatternException;
import org.eclipselabs.real.core.regex.IMatcherWrapper;
import org.eclipselabs.real.core.regex.IRealRegex;
import org.eclipselabs.real.core.searchobject.crit.AcceptanceCriterionStage;
import org.eclipselabs.real.core.searchobject.crit.IAcceptanceCriterion;
import org.eclipselabs.real.core.searchobject.param.IReplaceableParam;
import org.eclipselabs.real.core.searchobject.param.ReplaceableParamKey;
import org.eclipselabs.real.core.searchresult.ISRComplexRegex;
import org.eclipselabs.real.core.searchresult.ISRComplexRegexView;
import org.eclipselabs.real.core.searchresult.SRComplexRegexImpl;
import org.eclipselabs.real.core.searchresult.resultobject.ISROComplexRegex;
import org.eclipselabs.real.core.searchresult.resultobject.ISROComplexRegexView;
import org.eclipselabs.real.core.searchresult.resultobject.SROComplexRegexImpl;
import org.eclipselabs.real.core.util.FindTextResult;
import org.eclipselabs.real.core.util.PerformanceUtils;

/**
 * The implementation of the complex regex class. The main method that is implemented is performSearch.
 *
 * During search the following operations occur:
 * - the replace table is calculated for this search (with values from groups up the hierarchy replaced correctly)
 * - a new search result object {@link ISRComplexRegex} is created
 * - the acceptance criteria list is cloned (some criteria may be modified during search)
 * - it is verified that the search may proceed (with acceptance guesses at the moment)
 * - the search loops through the main regexes, finds values and adds new search result objects
 *
 * This class uses manual garbage collection to reduce memory consumption. Search with regular expressions
 * is very memory intensive and the JVM garbage collector is not fast enough therefore during search hundreds of megabytes
 * can be consumed for the search alone.
 * search memory consumption
 *
 * @author Vadim Korkin
 *
 */
public class SOComplexRegexImpl extends KeyedComplexSearchObjectImpl<ISRComplexRegex, ISROComplexRegex,
        ISOComplexRegexView, ISRComplexRegexView, ISROComplexRegexView, String> implements ISOComplexRegex {
    private static final Logger log = LogManager.getLogger(SOComplexRegexImpl.class);

    protected volatile List<IRealRegex> mainRegexList = Collections.synchronizedList(new ArrayList<IRealRegex>());

    public SOComplexRegexImpl(String aName) {
        super(SearchObjectType.COMPLEX_REGEX, aName);
    }

    @Override
    public ISRComplexRegex performSearch(PerformSearchRequest request) throws IncorrectPatternException {
        log.info("performSearch " + this);
        log.info("performSearch request " + request);
        if ((request == null) || (request.getProgressMonitor() == null)) {
            log.error("performSearch cannot proceed with request " + request);
            return null;
        }
        Integer finalRegexFlags = regexFlags;
        if (request.getCustomRegexFlags() != null) {
            finalRegexFlags = request.getCustomRegexFlags();
        }
        request.getProgressMonitor().setCurrentSOName(getSearchObjectName());
        // init the search result
        Map<String,String> cachedReplaceTable = getFinalReplaceTable(request.getStaticReplaceParams(), request.getDynamicReplaceParams());
        Map<ReplaceableParamKey, IReplaceableParam<?>> allReplaceParams = getAllReplaceParams(request.getStaticReplaceParams());
        ISRComplexRegex result = new SRComplexRegexImpl(getSearchObjectName(), getCloneSortRequestList(),
                cachedReplaceTable, request.getStaticReplaceParams(), allReplaceParams, getSearchObjectGroup(), getSearchObjectTags());
        result.setViewOrder(viewOrder);
        if (request.getCustomRegexFlags() != null) {
            result.setRegexFlags(request.getCustomRegexFlags());
        }
        if (getDateInfos() != null) {
            try {
                List<ISearchObjectDateInfo> newInfos = new ArrayList<>();
                for (ISearchObjectDateInfo di : getDateInfos()) {
                    newInfos.add(di.clone());
                }
                result.setDateInfos(newInfos);
            } catch (CloneNotSupportedException e) {
                log.error("performSearch",e);
            }
        }
        // clone all stages after SEARCH (not containing search)
        List<IAcceptanceCriterion> mergeAC = getCloneAcceptanceList(new Predicate<IAcceptanceCriterion>() {

            @Override
            public boolean test(IAcceptanceCriterion t) {
                return (((t.getStages().size() == 1) && (t.getStages().contains(AcceptanceCriterionStage.MERGE)))
                        || ((t.getStages().contains(AcceptanceCriterionStage.MERGE)) && (!t.isAccumulating())));
            }
        });
        if (mergeAC != null) {
            result.getAcceptanceList().addAll(mergeAC);
        }
        /* make a list of acceptance clones for the SEARCH stage because the acceptance objects
         * may be changed during the search (some criteria may accumulate results to perform certain functions)
         */
        List<IAcceptanceCriterion> searchAC = getCloneAcceptanceList(new Predicate<IAcceptanceCriterion>() {

            @Override
            public boolean test(IAcceptanceCriterion t) {
                return t.getStages().contains(AcceptanceCriterionStage.SEARCH);
            }
        });
        /* add to the result acceptances as well to save the accumulated information
         * (some acceptances may accumulate information) while accepting
         * for example storing distinct values for example.
         */
        if (searchAC != null) {
            for (IAcceptanceCriterion ac : searchAC) {
                if ((ac.getStages().contains(AcceptanceCriterionStage.MERGE)) && (ac.isAccumulating())) {
                    result.getAcceptanceList().add(ac);
                }
            }
        }
        // the default GC max count if 1000
        int gcMaxCount = PerformanceUtils.getIntProperty(ISearchObjectConstants.PERF_CONST_MAX_GC_COUNT, 1000);
        if (SearchObjectUtil.isSearchProceed(request.getText(), searchAC, result)) {
            String lastFound = null;
            int gcCount = 0;
            // according to Java documentation for the synchronized list it is
            // "imperative that the user manually synchronize on the returned list when iterating over it"
            // like this "synchronized(mainRegexList) {". But in this case the synchronization has been removed to make
            // searches faster because the regexes are not modified in the loop and the list itself is not modified
            // The speedup is noticeable because each file is processed in its own thread
            for (IRealRegex currRegex : mainRegexList) {
                try {
                    IMatcherWrapper mtw = currRegex.getMatcherWrapper(request.getText(), cachedReplaceTable, finalRegexFlags);
                    while (mtw.find()) {
                        FindTextResult foundStr = mtw.getResult();
                        ISROComplexRegex newSR = new SROComplexRegexImpl(foundStr.getStrResult(),
                                foundStr.getStartPos(), foundStr.getEndPos(), SearchObjectUtil.parseDate(getDateInfos(),
                                        foundStr.getStrResult(), cachedReplaceTable, finalRegexFlags));
                        if (newSR.getDate() != null) {
                            result.getFoundYears().add(newSR.getDate().getYear());
                        }
                        for (ISOComplexRegexView currView : viewMap.values()) {
                            newSR.addView(currView.getSearchObjectName(),
                                    currView.performSearch(new PerformSearchRequest(newSR.getText(), cachedReplaceTable, null)));
                        }
                        boolean acceptancePassed = SearchObjectUtil.accept(newSR, searchAC, result);
                        if (acceptancePassed) {
                            result.addSRObject(newSR);
                            request.getProgressMonitor().incrementObjectsFound();
                        }
                        lastFound = foundStr.getStrResult();
                        // manual GC is necessary to keep the heap size minimal
                        // during a search the garbage collector is not fast enough therefore
                        // the heap may grow too large
                        if (gcCount > gcMaxCount) {
                            System.gc();
                            gcCount = 0;
                            /*
                             * Also check if the thread is not interrupted. If interrupted this means
                             * maybe the timeout for this operation has expired and the future has been canceled.
                             * Anyway if the thread is interrupted exit immediately.
                             */
                            if (Thread.currentThread().isInterrupted()) {
                                log.error("Thread interrupted for the search " + this.getSearchObjectName());
                                return null;
                            }
                        }
                        gcCount++;
                    }
                } catch (Exception e) {
                    log.error("Caught exception last statement found " + lastFound
                            + " regex searched " + currRegex.getPatternString(cachedReplaceTable), e);
                    throw e;
                }
            }
        }
        if (result.getSRObjects().isEmpty()) {
            result = null;
        }
        request.getProgressMonitor().incrementCompletedWork();
        return result;
    }

    @Override
    public List<IRealRegex> getMainRegexList() {
        return mainRegexList;
    }

    @Override
    public void setMainRegexList(List<IRealRegex> mrList) {
        mainRegexList = mrList;
    }

    @Override
    public ISOComplexRegex clone() throws CloneNotSupportedException {
        SOComplexRegexImpl cloneObj = (SOComplexRegexImpl)super.clone();
        if (viewMap != null) {
            Map<String, ISOComplexRegexView> newViewMap = new ConcurrentHashMap<>();
            for (Map.Entry<String, ISOComplexRegexView> currView : viewMap.entrySet()) {
                newViewMap.put(currView.getKey(), (ISOComplexRegexView)currView.getValue().clone());
            }
            cloneObj.viewMap = newViewMap;
        }
        if (viewOrder != null) {
            List<String> newViewOrder = Collections.synchronizedList(new ArrayList<String>());
            newViewOrder.addAll(viewOrder);
            cloneObj.viewOrder = newViewOrder;
        }
        if (mainRegexList != null) {
            List<IRealRegex> newMainRegexes = Collections.synchronizedList(new ArrayList<IRealRegex>());
            // according to Java documentation for the synchronized list it is
            // "imperative that the user manually synchronize on the returned list
            // when iterating over it"
            synchronized(mainRegexList) {
                for (IRealRegex currRegex : mainRegexList) {
                    newMainRegexes.add(currRegex.clone());
                }
            }
            cloneObj.setMainRegexList(newMainRegexes);
        }
        return cloneObj;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("\nSOComplexRegexImpl name=" + getSearchObjectName() + " group=" + getSearchObjectGroup() + " tags=" + getSearchObjectTags());
        sb.append("\nReplaceParams ");
        if (getCloneParamList() != null) {
            for (IReplaceableParam<?> currParam : getCloneParamList()) {
                sb.append("\n").append(currParam);
            }
        }
        sb.append("\nDate info ").append(getDateInfos());
        sb.append("\nMain regexes:");
        for (IRealRegex currReg : mainRegexList) {
            sb.append("\n\t").append(currReg);
        }
        sb.append("\nAcceptance:");
        for (IAcceptanceCriterion currCrit : acceptanceList) {
            sb.append("\n\t").append(currCrit);
        }
        sb.append("\nViews:");
        for (ISOComplexRegexView currView : viewMap.values()) {
            sb.append("\n\t").append(currView);
        }
        return sb.toString();
    }

}
