package org.eclipselabs.real.core.searchobject;

import java.text.ParsePosition;
import java.time.DateTimeException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoField;
import java.time.temporal.TemporalAccessor;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipselabs.real.core.exception.IncorrectPatternException;
import org.eclipselabs.real.core.regex.IMatcherWrapper;
import org.eclipselabs.real.core.regex.IRealRegex;
import org.eclipselabs.real.core.util.IRealCoreConstants;

public class SearchObjectDateInfoImpl implements ISearchObjectDateInfo {

    private static final Logger log = LogManager.getLogger(SearchObjectDateInfoImpl.class);
    protected String dateFormat;
    protected List<IRealRegex> regexList;
    protected List<Locale> possibleLocales;

    public SearchObjectDateInfoImpl() {}

    public SearchObjectDateInfoImpl(String sdf, List<IRealRegex> regList) {
        dateFormat = sdf;
        regexList = regList;
    }

    public SearchObjectDateInfoImpl(String sdf, List<IRealRegex> regList, List<String> localeLanguages) {
        this(sdf, regList);
        if (localeLanguages != null) {
            List<Locale> localeList = new ArrayList<>();
            for (String langStr : localeLanguages) {
                Locale newLoc = new Locale(langStr);
                localeList.add(newLoc);
            }
            if (!localeList.isEmpty()) {
                possibleLocales = localeList;
            }
        }
    }

    public LocalDateTime parseDate(String logRecordStr, Map<String,String> replaceTable, Integer regexFlags) throws IncorrectPatternException {
        LocalDateTime cald = null;
        String dateStr = getDateString(logRecordStr, replaceTable, regexFlags);
        if (dateStr != null) {
            List<Locale> possibleLocaleValues = possibleLocales;
            if ((possibleLocaleValues == null) || (possibleLocaleValues.isEmpty())) {
                possibleLocaleValues = Collections.singletonList(IRealCoreConstants.DEFAULT_DATE_LOCALE);
            }
            String dateFtmStr = dateFormat;
            if ((replaceTable != null) && (!replaceTable.isEmpty())) {
                for (Map.Entry<String,String> currEntry : replaceTable.entrySet()) {
                    dateFtmStr = dateFtmStr.replace(currEntry.getKey(), currEntry.getValue());
                }
            }
            for (Locale currLocale : possibleLocaleValues) {
                DateTimeFormatter dtf = DateTimeFormatter.ofPattern(dateFtmStr, currLocale);
                try {
                    // do not parse completely because some important fields like the year may be unavailable
                    TemporalAccessor interimValue = dtf.parseUnresolved(dateStr, new ParsePosition(0));
                    if (interimValue == null) {
                        // this could be a wrong Locale try parsing with the next one
                        continue;
                    }
                    int year = getYear(interimValue);
                    int nanoSecs = getNanoseconds(interimValue);

                    cald = LocalDateTime.of(year, interimValue.get(ChronoField.MONTH_OF_YEAR), interimValue.get(ChronoField.DAY_OF_MONTH),
                            interimValue.get(ChronoField.HOUR_OF_DAY), interimValue.get(ChronoField.MINUTE_OF_HOUR),
                            interimValue.get(ChronoField.SECOND_OF_MINUTE), nanoSecs);
                } catch (DateTimeParseException e) {
                    log.error("Exception parsing date for text " + logRecordStr,e);
                }
            }

        }
        return cald;
    }

    private String getDateString(String initialLogString, Map<String,String> replaceTable, Integer regexFlags) throws IncorrectPatternException {
        String dateStr = null;
        StringBuilder sb = new StringBuilder();
        for (IRealRegex currReg : regexList) {
            IMatcherWrapper mtwr = currReg.getMatcherWrapper(initialLogString, replaceTable, regexFlags);
            while(mtwr.find()) {
                sb.append(mtwr.getResult().getStrResult());
            }
        }
        if (sb.length() > 0) {
            dateStr = sb.toString();
        }
        return dateStr;
    }

    private int getYear(TemporalAccessor partialValue) {
        // the default value as some log records may have no year at all
        int year = ISearchObjectConstants.DEFAULT_NOT_FOUND_YEAR;
        try {
            year = partialValue.get(ChronoField.YEAR_OF_ERA);
        } catch (DateTimeException e) {
            // these errors may be common do not print them
        }
        return year;
    }

    private int getNanoseconds(TemporalAccessor partialValue) {
        // the default value for the milliseconds as some log entries may have no milliseconds at all
        int nanoSecs = 0;
        try {
            nanoSecs = partialValue.get(ChronoField.NANO_OF_SECOND);
        } catch (DateTimeException e) {
            // these errors may be common do not print them
        }
        return nanoSecs;
    }

    @Override
    public String getDateFormat() {
        return dateFormat;
    }

    @Override
    public void setDateFormat(String dateFormat) {
        this.dateFormat = dateFormat;
    }

    @Override
    public List<IRealRegex> getRegexList() {
        return regexList;
    }

    @Override
    public void setRegexList(List<IRealRegex> regexList) {
        this.regexList = regexList;
    }

    @Override
    public List<Locale> getPossibleLocales() {
        return possibleLocales;
    }

    @Override
    public void setPossibleLocales(List<Locale> possibleLocales) {
        this.possibleLocales = possibleLocales;
    }

    @Override
    public ISearchObjectDateInfo clone() throws CloneNotSupportedException {
        SearchObjectDateInfoImpl cloneObj = (SearchObjectDateInfoImpl)super.clone();
        if (regexList != null) {
            List<IRealRegex> newRegexList = new ArrayList<>();
            for (IRealRegex currReg : regexList) {
                if (currReg != null) {
                    newRegexList.add(currReg.clone());
                }
            }
            cloneObj.setRegexList(newRegexList);
        }
        return cloneObj;
    }

    @Override
    public String toString() {
        return "SearchObjectDateInfoImpl [dateFormat=" + dateFormat + ", regexList=" + regexList + "]";
    }
}
