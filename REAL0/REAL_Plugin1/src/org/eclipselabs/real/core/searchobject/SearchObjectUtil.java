package org.eclipselabs.real.core.searchobject;

import java.text.ParsePosition;
import java.time.DateTimeException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoField;
import java.time.temporal.TemporalAccessor;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipselabs.real.core.exception.IncorrectPatternException;
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

    public static LocalDateTime parseDate(List<ISearchObjectDateInfo> dateInfos, String dateStr, Map<String,String> replaceTable,
            Integer regexFlags) throws IncorrectPatternException {
        LocalDateTime parsedDate = null;
        if ((dateInfos != null) && (!dateInfos.isEmpty())) {
            /*
             * Find the first working date info from the list and use it
             */
            for (ISearchObjectDateInfo info : dateInfos) {
                parsedDate = parseDateFromInfo(info, dateStr, replaceTable, regexFlags);
                if (parsedDate != null) {
                    break;
                }
            }
        }
        return parsedDate;
    }

    public static LocalDateTime parseDateFromInfo(ISearchObjectDateInfo dateInfo, String dateStr, Map<String,String> replaceTable,
            Integer regexFlags) throws IncorrectPatternException {
        LocalDateTime cald = null;
        if (dateInfo != null) {
            StringBuilder sb = new StringBuilder();
            for (IRealRegex currReg : dateInfo.getRegexList()) {
                IMatcherWrapper mtwr = currReg.getMatcherWrapper(dateStr, replaceTable, regexFlags);
                while(mtwr.find()) {
                    sb.append(mtwr.getResult().getStrResult());
                }
            }
            if (sb.length() > 0) {
                String dateFtmStr = dateInfo.getDateFormat();
                if ((replaceTable != null) && (!replaceTable.isEmpty())) {
                    for (Map.Entry<String,String> currEntry : replaceTable.entrySet()) {
                        dateFtmStr = dateFtmStr.replace(currEntry.getKey(), currEntry.getValue());
                    }
                }
                DateTimeFormatter dtf = DateTimeFormatter.ofPattern(dateFtmStr, IRealCoreConstants.DEFAULT_DATE_LOCALE);
                try {
                    // do not parse completely because some important fields like the year may be unavailable
                    TemporalAccessor interimValue = dtf.parseUnresolved(sb.toString(), new ParsePosition(0));
                    // the default value as some log records may have no year at all
                    int year = ISearchObjectConstants.DEFAULT_NOT_FOUND_YEAR;
                    try {
                        year = interimValue.get(ChronoField.YEAR_OF_ERA);
                    } catch (DateTimeException e) {
                        // these errors may be common do not print them
                    }
                    // the default value for the milliseconds as some log entries may have no milliseconds at all
                    int nanoSecs = 0;
                    try {
                        nanoSecs = interimValue.get(ChronoField.NANO_OF_SECOND);
                    } catch (DateTimeException e) {
                        // these errors may be common do not print them
                    }
                    cald = LocalDateTime.of(year, interimValue.get(ChronoField.MONTH_OF_YEAR), interimValue.get(ChronoField.DAY_OF_MONTH),
                            interimValue.get(ChronoField.HOUR_OF_DAY), interimValue.get(ChronoField.MINUTE_OF_HOUR),
                            interimValue.get(ChronoField.SECOND_OF_MINUTE), nanoSecs);
                } catch (DateTimeParseException e) {
                    log.error("Exception parsing date for text " + dateStr,e);
                }
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
