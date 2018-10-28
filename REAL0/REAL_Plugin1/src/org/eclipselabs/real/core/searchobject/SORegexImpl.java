package org.eclipselabs.real.core.searchobject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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
import org.eclipselabs.real.core.searchresult.ISRRegex;
import org.eclipselabs.real.core.searchresult.SRRegex;
import org.eclipselabs.real.core.searchresult.resultobject.ISRORegex;
import org.eclipselabs.real.core.searchresult.resultobject.SRORegexImpl;
import org.eclipselabs.real.core.util.FindTextResult;

public class SORegexImpl extends KeyedSearchObjectImpl<ISRRegex, ISRORegex> implements ISORegex {

    private static final Logger log = LogManager.getLogger(SORegexImpl.class);
    protected IRealRegex theRegex;

    public SORegexImpl(String aName) {
        super(SearchObjectType.SIMPLE_REGEX, aName);
    }

    public SORegexImpl(String aName, IRealRegex aRegex) {
        super(SearchObjectType.SIMPLE_REGEX, aName);
        theRegex = aRegex;
    }

    @Override
    public ISRRegex performSearch(PerformSearchRequest request) throws IncorrectPatternException {
        log.debug("performSearch " + this);
        log.info("performSearch request " + request);
        if (request == null) {
            log.error("performSearch request is null");
            return null;
        }
        request.getProgressMonitor().setCurrentSOName(getSearchObjectName());
        // init the search result
        Map<String,String> cachedReplaceTable = getFinalReplaceTable(request.getStaticReplaceParams(), request.getDynamicReplaceParams());
        Map<ReplaceableParamKey, IReplaceableParam<?>> allReplaceParams = getAllReplaceParams(request.getStaticReplaceParams());
        Integer finalRegexFlags = regexFlags;
        if (request.getCustomRegexFlags() != null) {
            finalRegexFlags = request.getCustomRegexFlags();
        }
        ISRRegex result = new SRRegex(getSearchObjectName(), getCloneSortRequestList(), cachedReplaceTable,
                request.getStaticReplaceParams(), allReplaceParams, getSearchObjectGroup(), getSearchObjectTags());
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

        if (SearchObjectUtil.isSearchProceed(request.getText(), searchAC, result)) {
            IMatcherWrapper mwr = theRegex.getMatcherWrapper(request.getText(), cachedReplaceTable, finalRegexFlags);
            while (mwr.find()) {
                FindTextResult res = mwr.getResult();
                ISRORegex newSR = new SRORegexImpl(res.getStrResult(), res.getStartPos(), res.getEndPos(),
                        SearchObjectUtil.parseDate(getDateInfos(), res.getStrResult(), cachedReplaceTable, finalRegexFlags));
                boolean acceptancePassed = SearchObjectUtil.accept(newSR, searchAC, result);
                if (acceptancePassed) {
                    result.addSRObject(newSR);
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
    public IRealRegex getRegex() {
        return theRegex;
    }
    @Override
    public void setRegex(IRealRegex regex) {
        theRegex = regex;
    }

    @Override
    public ISORegex clone() throws CloneNotSupportedException {
        SORegexImpl cloneObj = (SORegexImpl)super.clone();
        if (theRegex != null) {
            cloneObj.setRegex(theRegex.clone());
        }
        return cloneObj;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("SORegexImpl name=").append(getSearchObjectName()).append(" group=").append(getSearchObjectGroup()).append(" tags=").append(getSearchObjectTags());
        sb.append("\nReplaceParams ").append(getCloneParamList());
        sb.append("\nDate info ").append(getDateInfos());
        sb.append("\nMainRegex ").append(theRegex);
        sb.append("\nAcceptance:");
        for (IAcceptanceCriterion currCrit : acceptanceList) {
            sb.append("\n\t").append(currCrit);
        }
        return sb.toString();
    }

}
