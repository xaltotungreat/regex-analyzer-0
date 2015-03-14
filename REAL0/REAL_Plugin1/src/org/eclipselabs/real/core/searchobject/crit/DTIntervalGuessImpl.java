package org.eclipselabs.real.core.searchobject.crit;

import java.util.Calendar;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipselabs.real.core.regex.IMatcherWrapper;
import org.eclipselabs.real.core.regex.IRealRegex;
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
            IDTIntervalCriterion currCriterion = (IDTIntervalCriterion)criterion;
            result = new AcceptanceGuessResult(currCriterion.getName(), currCriterion.getType());
            Calendar lowBound = currCriterion.getLowBound();
            Calendar highBound = currCriterion.getHighBound();
            
            IMatcherWrapper fstMwr = firstRecord.getMatcherWrapper(logText, sr.getCachedReplaceTable(), sr.getRegexFlags());
            Calendar fstDate = null;
            if (fstMwr.find()) {
                FindTextResult fstRes = fstMwr.getResult();
                fstDate = SearchObjectUtil.parseDate(sr.getDateInfo(), fstRes.getStrResult(), 
                        sr.getCachedReplaceTable(), sr.getRegexFlags());
            }
            
            IMatcherWrapper lstMwr = lastRecord.getMatcherWrapper(logText, sr.getCachedReplaceTable(), sr.getRegexFlags());
            Calendar lstDate = null;
            if (lstMwr.find()) {
                FindTextResult lstRes = lstMwr.getResult();
                lstDate = SearchObjectUtil.parseDate(sr.getDateInfo(), lstRes.getStrResult(), 
                        sr.getCachedReplaceTable(), sr.getRegexFlags());
            }
            if ((fstDate != null) && (lstDate != null)) {
                if ((fstDate.get(Calendar.YEAR) == 1970) || (lstDate.get(Calendar.YEAR) == 1970)) {
                    /* to guard against a possible new year issue try to get the fist date year
                     * from the low bound and last record year from the high bound 
                     */
                    if (fstDate.get(Calendar.YEAR) == 1970) {
                        if (fstDate.get(Calendar.MONTH) == highBound.get(Calendar.MONTH)) {
                            fstDate.set(Calendar.YEAR, highBound.get(Calendar.YEAR));
                        } else {
                            fstDate.set(Calendar.YEAR, lowBound.get(Calendar.YEAR));
                        }
                    }
                    if (lstDate.get(Calendar.YEAR) == 1970) {
                        if (lstDate.get(Calendar.MONTH) == lowBound.get(Calendar.MONTH)) {
                            lstDate.set(Calendar.YEAR, lowBound.get(Calendar.YEAR));
                        } else {
                            lstDate.set(Calendar.YEAR, highBound.get(Calendar.YEAR));
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
