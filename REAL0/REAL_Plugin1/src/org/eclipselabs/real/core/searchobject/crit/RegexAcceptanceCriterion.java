package org.eclipselabs.real.core.searchobject.crit;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipselabs.real.core.regex.IMatcherWrapper;
import org.eclipselabs.real.core.regex.IRealRegex;
import org.eclipselabs.real.core.searchresult.ISearchResult;
import org.eclipselabs.real.core.searchresult.resultobject.ISearchResultObject;
import org.eclipselabs.real.core.util.FindTextResult;

public class RegexAcceptanceCriterion extends AcceptanceCriterionImpl implements IRegexAcceptanceCriterion {

    private static final Logger log = LogManager.getLogger(RegexAcceptanceCriterion.class);
    protected List<IRealRegex> acceptanceRegexList = Collections.synchronizedList(new ArrayList<IRealRegex>());
    protected Map<IRealRegex, Set<String>> distinctResultsMap = new ConcurrentHashMap<IRealRegex, Set<String>>();

    public RegexAcceptanceCriterion(AcceptanceCriterionType aType) {
        this(aType, null);
    }

    public RegexAcceptanceCriterion(AcceptanceCriterionType aType, String newName) {
        super(aType, newName, AcceptanceCriterionStage.SEARCH);
        if (AcceptanceCriterionType.DISTINCT.equals(aType)) {
            stages.add(AcceptanceCriterionStage.MERGE);
            accumulating = true;
        }
    }

    @Override
    public void init(ISearchResult<? extends ISearchResultObject> sr) {
        // do nothing
    }

    @Override
    public boolean test(ISearchResultObject sro, ISearchResult<? extends ISearchResultObject> sr) {
        boolean result = true;
        if ((sro != null) && (sr != null)) {
            if (acceptanceRegexList != null) {
                for (IRealRegex currAcceptRegex : acceptanceRegexList) {
                    IMatcherWrapper mwr = null;
                    switch(type) {
                    case FIND:
                        mwr = currAcceptRegex.getMatcherWrapper(sro.getText(), sr.getCachedReplaceTable(), sr.getRegexFlags());
                        result = mwr.find();
                        break;
                    case FIND_NULLABLE:
                        // for nullables if the pattern is 0-length after the parameter is replaced
                        // the criterion is considered satisfied - special case
                        if (!currAcceptRegex.getPatternString(sr.getCachedReplaceTable()).isEmpty()) {
                            mwr = currAcceptRegex.getMatcherWrapper(sro.getText(), sr.getCachedReplaceTable(), sr.getRegexFlags());
                            result = mwr.find();
                        }
                        break;
                    case MATCH:
                        mwr = currAcceptRegex.getMatcherWrapper(sro.getText(), sr.getCachedReplaceTable(), sr.getRegexFlags());
                        result = mwr.matches();
                        break;
                    case NOT_FIND:
                        mwr = currAcceptRegex.getMatcherWrapper(sro.getText(), sr.getCachedReplaceTable(), sr.getRegexFlags());
                        result = !mwr.find();
                        break;
                    case NOT_FIND_NULLABLE:
                        // for nullables if the pattern is 0-length after the parameter is replaced
                        // the criterion is considered satisfied - special case
                        if (!currAcceptRegex.getPatternString(sr.getCachedReplaceTable()).isEmpty()) {
                            mwr = currAcceptRegex.getMatcherWrapper(sro.getText(), sr.getCachedReplaceTable(), sr.getRegexFlags());
                            result = !mwr.find();
                        }
                        break;
                    case NOT_MATCH:
                        mwr = currAcceptRegex.getMatcherWrapper(sro.getText(), sr.getCachedReplaceTable(), sr.getRegexFlags());
                        result = !mwr.matches();
                        break;
                    case DISTINCT:
                        result = verifyDistinct(sro, sr, currAcceptRegex);
                        break;
                    default:
                        log.error("test IncorrectType " + type + " for a regex criterion");
                        break;
                    }
                    if (!result) {
                        break;
                    }
                }
            } else {
                log.error("test acceptanceRegex is null returning true");
            }
        } else {
            log.warn("test passed sro " + sro + " or sr " + sr + " is null. Return true");
        }
        return result;
    }

    protected boolean verifyDistinct(ISearchResultObject sro, ISearchResult<? extends ISearchResultObject> sr, IRealRegex currRegex) {
        boolean result = false;
            Set<String> regexDistinctResults = distinctResultsMap.get(currRegex);
            if (regexDistinctResults == null) {
                regexDistinctResults = new HashSet<>();
                distinctResultsMap.put(currRegex, regexDistinctResults);
            }
            IMatcherWrapper selectionWrapper = currRegex.getMatcherWrapper(sro.getText(), sr.getCachedReplaceTable(), sr.getRegexFlags());
            while (selectionWrapper.find()) {
                FindTextResult currResult = selectionWrapper.getResult();
                if ((currResult.getStrResult() != null) && (!regexDistinctResults.contains(currResult.getStrResult()))) {
                    result = true;
                    regexDistinctResults.add(currResult.getStrResult());
                    break;
                }
            }
        return result;
    }

    @Override
    public List<IRealRegex> getAcceptanceRegex() {
        return acceptanceRegexList;
    }

    @Override
    public void setAcceptanceRegex(List<IRealRegex> acceptanceRegex) {
        this.acceptanceRegexList = acceptanceRegex;
    }

    public Map<IRealRegex, Set<String>> getDistinctResultsMap() {
        return distinctResultsMap;
    }

    public void setDistinctResultsMap(Map<IRealRegex, Set<String>> distinctResultsMap) {
        this.distinctResultsMap = distinctResultsMap;
    }

    @Override
    public IAcceptanceCriterion clone() throws CloneNotSupportedException {
        RegexAcceptanceCriterion cloneObj = (RegexAcceptanceCriterion)super.clone();
        if (acceptanceRegexList != null) {
            List<IRealRegex> clonedList = Collections.synchronizedList(new ArrayList<IRealRegex>());
            Map<IRealRegex, Set<String>> clonedDistinctResultsMap = null;
            if (distinctResultsMap != null) {
                clonedDistinctResultsMap = new ConcurrentHashMap<IRealRegex, Set<String>>();
            }
            synchronized (acceptanceRegexList) {
                for (IRealRegex currRegex : acceptanceRegexList) {
                    IRealRegex currClone = currRegex.clone();
                    clonedList.add(currClone);
                    if (clonedDistinctResultsMap != null) {
                        Set<String> currRegSet = distinctResultsMap.get(currRegex);
                        if (currRegSet != null) {
                            Set<String> clonedSet = new HashSet<>();
                            clonedSet.addAll(currRegSet);
                            clonedDistinctResultsMap.put(currClone, clonedSet);
                        }
                    }
                }
            }
            cloneObj.setAcceptanceRegex(clonedList);
            cloneObj.distinctResultsMap = clonedDistinctResultsMap;
        }

        return cloneObj;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("RegexAcceptanceCriterion [type=" + type + ", name=" + name);
        sb.append("\nGuessList");
        for (IAcceptanceGuess ag : guessList) {
            sb.append("\n\t" + ag);
        }
        sb.append("\nRegexList");
        for (IRealRegex currRegex : acceptanceRegexList) {
            sb.append("\n\t " + currRegex);
        }
        return sb.toString();
    }



}
