package org.eclipselabs.real.core.searchobject.crit;

import java.time.LocalDateTime;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipselabs.real.core.regex.IMatcherWrapper;
import org.eclipselabs.real.core.regex.IRealRegex;
import org.eclipselabs.real.core.searchobject.ISearchObjectConstants;
import org.eclipselabs.real.core.searchobject.SearchObjectUtil;
import org.eclipselabs.real.core.searchresult.ISearchResult;
import org.eclipselabs.real.core.searchresult.resultobject.ISearchResultObject;
import org.eclipselabs.real.core.util.FindTextResult;

public class DTIntervalGuessImpl extends AcceptanceGuessImpl implements IDTIntervalGuess {

    private static final Logger log = LogManager.getLogger(DTIntervalGuessImpl.class);
    protected IRealRegex firstRecord;
    protected IRealRegex lastRecord;

    public DTIntervalGuessImpl() {
    }

    public DTIntervalGuessImpl(String aName) {
        super(aName);
    }

    @Override
    public AcceptanceGuessResult getGuessResult(String logText, ISearchResult<? extends ISearchResultObject> sr) {
        AcceptanceGuessResult result = null;
        boolean proceed = true;
        if (proceed && ((firstRecord == null) || (lastRecord == null))) {
            log.error("getGuessResult first record " + firstRecord +
                    " or last record " + lastRecord + " is null");
            proceed = false;
        }
        if (proceed && ((logText == null) || (sr == null))) {
            log.error("getGuessResult logText or search result is null");
            proceed = false;
        }
        if (proceed) {
            IDTIntervalCriterion currCriterion = (IDTIntervalCriterion)acceptanceCriterion;
            result = new AcceptanceGuessResult(currCriterion.getName(), currCriterion.getType());
            LocalDateTime lowBound = currCriterion.getLowBound();
            LocalDateTime highBound = currCriterion.getHighBound();

            IMatcherWrapper fstMwr = firstRecord.getMatcherWrapper(logText, sr.getCachedReplaceTable(), sr.getRegexFlags());
            LocalDateTime fstDate = null;
            if (fstMwr.find()) {
                FindTextResult fstRes = fstMwr.getResult();
                fstDate = SearchObjectUtil.parseDate(sr.getDateInfo(), fstRes.getStrResult(),
                        sr.getCachedReplaceTable(), sr.getRegexFlags());
            }

            IMatcherWrapper lstMwr = lastRecord.getMatcherWrapper(logText, sr.getCachedReplaceTable(), sr.getRegexFlags());
            LocalDateTime lstDate = null;
            if (lstMwr.find()) {
                FindTextResult lstRes = lstMwr.getResult();
                lstDate = SearchObjectUtil.parseDate(sr.getDateInfo(), lstRes.getStrResult(),
                        sr.getCachedReplaceTable(), sr.getRegexFlags());
            }
            if ((fstDate != null) && (lstDate != null)) {
                if ((fstDate.getYear() == ISearchObjectConstants.DEFAULT_NOT_FOUND_YEAR) || (lstDate.getYear() == ISearchObjectConstants.DEFAULT_NOT_FOUND_YEAR)) {
                    /* to guard against a possible new year issue try to get the years
                     * from the bounds. it is highly unlikely that the month of
                     * the low bound and the month of the high bound differ by more than 1.
                     */
                    if (fstDate.getYear() == ISearchObjectConstants.DEFAULT_NOT_FOUND_YEAR) {
                        if (fstDate.getMonth() == highBound.getMonth()) {
                            fstDate = fstDate.withYear(highBound.getYear());
                        } else {
                            fstDate = fstDate.withYear(lowBound.getYear());
                        }
                    }
                    if (lstDate.getYear() == ISearchObjectConstants.DEFAULT_NOT_FOUND_YEAR) {
                        if (lstDate.getMonth() == lowBound.getMonth()) {
                            lstDate = lstDate.withYear(lowBound.getYear());
                        } else {
                            lstDate = lstDate.withYear(highBound.getYear());
                        }
                    }
                }
                boolean shouldSearch = (((fstDate.compareTo(lowBound) >= 0) && (fstDate.compareTo(highBound) <= 0))
                        || ((lstDate.compareTo(lowBound) >= 0) && (lstDate.compareTo(highBound) <= 0))
                        || ((fstDate.compareTo(lowBound) <= 0) && (lstDate.compareTo(highBound) >= 0)));
                result.setContinueSearch(shouldSearch);
            }
        }
        return result;
    }

    @Override
    public IAcceptanceGuess clone() throws CloneNotSupportedException {
        DTIntervalGuessImpl cloneObj = (DTIntervalGuessImpl)super.clone();
        if (firstRecord != null) {
            cloneObj.setFirstRecord(firstRecord.clone());
        }
        if (lastRecord != null) {
            cloneObj.setLastRecord(lastRecord.clone());
        }
        return cloneObj;
    }

    @Override
    public IRealRegex getFirstRecord() {
        return firstRecord;
    }

    @Override
    public void setFirstRecord(IRealRegex fstRec) {
        firstRecord = fstRec;
    }

    @Override
    public IRealRegex getLastRecord() {
        return lastRecord;
    }

    @Override
    public void setLastRecord(IRealRegex lstRec) {
        lastRecord = lstRec;
    }

}
