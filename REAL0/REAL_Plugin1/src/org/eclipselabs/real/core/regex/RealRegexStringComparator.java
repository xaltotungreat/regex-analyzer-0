package org.eclipselabs.real.core.regex;

import java.text.Collator;
import java.util.Comparator;
import java.util.Map;

import org.eclipselabs.real.core.exception.IncorrectPatternException;
import org.eclipselabs.real.core.util.FindTextResult;

public class RealRegexStringComparator implements Comparator<String> {

    protected IRealRegex regex;
    protected Map<String,String> replaceTable;
    protected Integer regexFlags;

    public RealRegexStringComparator(IRealRegex reg, Map<String,String> replMap, Integer regFlags) {
        regex = reg;
        replaceTable = replMap;
        regexFlags = regFlags;
    }

    @Override
    public int compare(String o1, String o2) {
        int result = 0;
        if ((o1 != null) && (o2 != null)) {
            Collator coll = Collator.getInstance();
            try {
                IMatcherWrapper mwr1 = regex.getMatcherWrapper(o1, replaceTable, regexFlags);
                IMatcherWrapper mwr2 = regex.getMatcherWrapper(o2, replaceTable, regexFlags);
                FindTextResult res1 = null;
                FindTextResult res2 = null;
                if (mwr1.find()) {
                    res1 = mwr1.getResult();
                }
                if (mwr2.find()) {
                    res2 = mwr2.getResult();
                }
                if ((res1 != null) && (res2 != null)) {
                    result = coll.compare(res1.getStrResult(), res2.getStrResult());
                } else if ((res1 == null) && (res2 != null)) {
                    result = -1;
                } else if ((res1 != null) && (res2 == null)) {
                    result = 1;
                } else {
                    result = coll.compare(o1, o2);
                }
            } catch (IncorrectPatternException ipe) {
                // do nothingfor now, what should be done?
            }
        } else if ((o1 == null) && (o2 != null)) {
            result = -1;
        } else if ((o1 != null) && (o2 == null)) {
            result = 1;
        }
        return result;
    }

}
