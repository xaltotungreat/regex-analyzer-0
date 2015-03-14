package org.eclipselabs.real.core.searchresult.resultobject;

import java.text.Collator;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.eclipselabs.real.core.regex.IMatcherWrapper;
import org.eclipselabs.real.core.regex.IRealRegex;
import org.eclipselabs.real.core.util.FindTextResult;

public class SROTextComparator<C extends ISearchResultObject> implements Comparator<C> {

    protected List<IRealRegex> sortRegexList;
    protected Map<String,String> replaceTable;
    protected Integer regexFlags;
    
    public SROTextComparator(List<IRealRegex> reg, Map<String,String> replMap, Integer regFlags) {
        sortRegexList = reg;
        replaceTable = replMap;
        regexFlags = regFlags;
    }
    
    @Override
    public int compare(ISearchResultObject o1, ISearchResultObject o2) {
        int result = 0;
        if ((o1 != null) && (o2 != null)) {
            if ((sortRegexList != null) && (!sortRegexList.isEmpty())) {
                int currRegexResult = result;
                for (IRealRegex currSortRegex : sortRegexList) {
                    Collator coll = Collator.getInstance();
                    IMatcherWrapper mwr1 = currSortRegex.getMatcherWrapper(o1.getText(), replaceTable, regexFlags);
                    IMatcherWrapper mwr2 = currSortRegex.getMatcherWrapper(o2.getText(), replaceTable, regexFlags);
                    FindTextResult textRes1 = null;
                    FindTextResult textRes2 = null;
                    if (mwr1.find()) {
                        textRes1 = mwr1.getResult();
                    }
                    if (mwr2.find()) {
                        textRes2 = mwr2.getResult();
                    }
                    if ((textRes1 != null) && (textRes2 != null)) {
                        currRegexResult = coll.compare(textRes1.getStrResult(), textRes2.getStrResult());
                    } else if ((textRes1 == null) && (textRes2 != null)) {
                        currRegexResult = -1;
                    } else if ((textRes1 != null) && (textRes2 == null)) {
                        currRegexResult = 1;
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
