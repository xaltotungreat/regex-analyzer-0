package org.eclipselabs.real.core.searchobject;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipselabs.real.core.exception.IncorrectPatternException;
import org.eclipselabs.real.core.searchobject.crit.AcceptanceGuessResult;
import org.eclipselabs.real.core.searchobject.crit.IAcceptanceCriterion;
import org.eclipselabs.real.core.searchobject.crit.IAcceptanceGuess;
import org.eclipselabs.real.core.searchresult.ISearchResult;
import org.eclipselabs.real.core.searchresult.resultobject.ISearchResultObject;

public class SearchObjectUtil {

    private static final Logger log = LogManager.getLogger(SearchObjectUtil.class);

    private SearchObjectUtil() {}

    public static LocalDateTime parseDate(List<ISearchObjectDateInfo> dateInfos, String dateStr, Map<String,String> replaceTable,
            Integer regexFlags) throws IncorrectPatternException {
        LocalDateTime parsedDate = null;
        if ((dateInfos != null) && (!dateInfos.isEmpty())) {
            /*
             * Find the first working date info from the list and use it
             */
            for (ISearchObjectDateInfo info : dateInfos) {
                parsedDate = info.parseDate(dateStr, replaceTable, regexFlags);
                if (parsedDate != null) {
                    break;
                }
            }
        }
        return parsedDate;
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
            ISearchResult<? extends ISearchResultObject> sr) throws IncorrectPatternException {
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
