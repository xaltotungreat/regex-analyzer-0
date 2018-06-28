package org.eclipselabs.real.core.searchresult.resultobject;

import java.text.Collator;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipselabs.real.core.exception.IncorrectPatternException;
import org.eclipselabs.real.core.regex.IMatcherWrapper;
import org.eclipselabs.real.core.regex.IRealRegex;
import org.eclipselabs.real.core.searchresult.ISearchResult;
import org.eclipselabs.real.core.util.FindTextResult;

public class SROViewComparator<O extends IComplexSearchResultObject<W,X,Q>, W extends ISearchResult<X>, X extends ISearchResultObject,Q>
        implements Comparator<O> {

    private static final Logger log = LogManager.getLogger(SROViewComparator.class);
    protected List<IRealRegex> sortRegexList;
    protected Map<String,String> replaceTable;
    protected Q viewName;
    protected Integer regexFlags;

    public SROViewComparator(List<IRealRegex> reg, Map<String,String> replMap, Q viewName2, Integer regFlags) {
        sortRegexList = reg;
        replaceTable = replMap;
        viewName = viewName2;
        regexFlags = regFlags;
    }

    @Override
    public int compare(O o1, O o2) {
        int result = 0;
        if ((o1 != null) && (o2 != null)) {
            if ((sortRegexList != null) && (!sortRegexList.isEmpty())) {
                int currRegexResult = result;
                String o1Str = null;
                String o2Str = null;
                Collator coll = Collator.getInstance();
                for (IRealRegex currSortRegex : sortRegexList) {
                    o1Str = null;
                    o2Str = null;
                    if ((viewName != null) && (!"".equals(viewName))) {
                        o1Str = o1.getViewText(viewName);
                        o2Str = o2.getViewText(viewName);
                    }
                    if ((o1Str == null) || (o2Str == null)) {
                        o1Str = o1.getText();
                        o2Str = o2.getText();
                    }
                    try {
                    IMatcherWrapper mwr1 = currSortRegex.getMatcherWrapper(o1Str, replaceTable, regexFlags);
                    IMatcherWrapper mwr2 = currSortRegex.getMatcherWrapper(o2Str, replaceTable, regexFlags);
                    FindTextResult textRes1 = null;
                    FindTextResult textRes2 = null;
                    while (mwr1.find()) {
                        textRes1 = mwr1.getResult();
                        break;
                    }
                    while (mwr2.find()) {
                        textRes2 = mwr2.getResult();
                    }
                    if ((textRes1 != null) && (textRes2 != null)) {
                        currRegexResult = coll.compare(textRes1.getStrResult(), textRes2.getStrResult());
                    } else if ((textRes1 == null) && (textRes2 != null)) {
                        currRegexResult = -1;
                    } else if ((textRes1 != null) && (textRes2 == null)) {
                        currRegexResult = 1;
                    } else {
                        result =  coll.compare(o1Str, o2Str);
                    }
                    } catch (IncorrectPatternException e) {
                        /*
                         * The current regex is incorrect then log the exception and try the next regex.
                         * It maybe considered that the objects cannot be compared using this regex
                         * which means they are equal.
                         */
                        log.error("Incorrect regex for the text comparator ", e);
                    }
                    if (currRegexResult != result) {
                        result = currRegexResult;
                        break;
                    }
                }
            }
        } else if ((o1 == null) && (o2 != null)) {
            result = -1;
        } else if ((o1 != null) && (o2 == null)) {
            result = 1;
        }
        return result;
    }

}
