package org.eclipselabs.real.core.searchobject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipselabs.real.core.regex.IMatcherWrapper;
import org.eclipselabs.real.core.regex.IRealRegex;
import org.eclipselabs.real.core.searchobject.crit.AcceptanceGuessResult;
import org.eclipselabs.real.core.searchobject.crit.IAcceptanceCriterion;
import org.eclipselabs.real.core.searchobject.crit.IAcceptanceGuess;
import org.eclipselabs.real.core.searchresult.ISearchResult;
import org.eclipselabs.real.core.searchresult.resultobject.ISearchResultObject;
import org.eclipselabs.real.core.util.IRealCoreConstants;

public class SearchObjectUtil {

    private static final Logger log = LogManager.getLogger(SearchObjectUtil.class);
    
    private SearchObjectUtil() {}
    
    public static Calendar parseDate(ISearchObjectDateInfo dateInfo, String dateStr, Map<String,String> replaceTable, 
            Integer regexFlags) {
        Calendar cald = null;
        if (dateInfo != null) {
            StringBuilder sb = new StringBuilder();
            for (IRealRegex currReg : dateInfo.getRegexList()) {
                IMatcherWrapper mtwr = currReg.getMatcherWrapper(dateStr, replaceTable, regexFlags);
                while(mtwr.find()) {
                    sb.append(mtwr.getResult().getStrResult());
                }
            }
            String dateFtmStr = dateInfo.getDateFormat();
            if ((replaceTable != null) && (!replaceTable.isEmpty())) {
                for (Map.Entry<String,String> currEntry : replaceTable.entrySet()) {
                    dateFtmStr = dateFtmStr.replace(currEntry.getKey(), currEntry.getValue());
                }
            }
            SimpleDateFormat sdf = new SimpleDateFormat(dateFtmStr, IRealCoreConstants.MAIN_DATE_LOCALE);
            try {
                Date dt = sdf.parse(sb.toString());
                cald = Calendar.getInstance();
                cald.setTime(dt);
            } catch (ParseException e) {
                log.error("Exception parsing date for text " + dateStr,e);
            }
        }
        return cald;
    }
    
    public static boolean accept(ISearchResultObject sro, List<IAcceptanceCriterion> acceptanceLst,
            ISearchResult<? extends ISearchResultObject> sr) {
        boolean result = true;
        if ((acceptanceLst != null) && (!acceptanceLst.isEmpty())) {
            for (IAcceptanceCriterion ac : acceptanceLst) {
                if (!ac.test(sro, sr)) {
                    result = false;
                    break;
                }
            }
        }
        return result;
    }
    
    public static boolean isSearchProceed(String logText, List<IAcceptanceCriterion> acceptanceLst,
            ISearchResult<? extends ISearchResultObject> sr) {
        boolean result = true;
        if ((acceptanceLst != null) && (!acceptanceLst.isEmpty())) {
            for (IAcceptanceCriterion ac : acceptanceLst) {
                if (ac.getGuessList() != null) {
                    ac.init(sr);
                    for (IAcceptanceGuess currGuess : ac.getGuessList()) {
                        AcceptanceGuessResult res = currGuess.getGuessResult(logText, sr);
                        if (!res.isContinueSearch()) {
                            result = false;
                            break;
                        }
                    } 
                }
                if (!result) {
                    break;
                }
            }
        }
        return result;
    }
    
}
