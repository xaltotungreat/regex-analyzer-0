package org.eclipselabs.real.core.regex;

import java.util.concurrent.LinkedBlockingDeque;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipselabs.real.core.util.FindTextResult;

class FindStrategyOnePatternLastInstance extends FindStrategyInstanceImpl {
    private static final Logger log = LogManager.getLogger(FindStrategyOnePatternLastInstance.class);

    protected Matcher oneMatcher;

    public FindStrategyOnePatternLastInstance(Pattern pt, String text, Integer resNum) {
        super(FindStrategyType.ONE_PATTERN_LAST_INSTANCE, text, resNum);
        if (pt != null) {
            oneMatcher = pt.matcher(text);
        }
    }

    @Override
    public boolean find() {
        boolean booleanResult = false;
        LinkedBlockingDeque<FindTextResult> storeLastResults = null;
        if (mainInstanceNumber >= 0) {
            storeLastResults = new LinkedBlockingDeque<FindTextResult>(mainInstanceNumber + 1);
            FindTextResult tmpResult;
            while (oneMatcher.find()) {
                tmpResult = new FindTextResult(oneMatcher.group(), oneMatcher.start(), oneMatcher.end());
                if (!storeLastResults.offerFirst(tmpResult)) {
                    storeLastResults.pollLast();
                    if (!storeLastResults.offerFirst(tmpResult)) {
                        log.error("find unknown error adding new result to the deque");
                    }
                }
            }
            currResult = storeLastResults.pollLast();
            booleanResult = (currResult != null);
        }
        return booleanResult;
    }

    @Override
    public boolean matches() {
        boolean matchVar = false;
        if (oneMatcher != null) {
            matchVar = oneMatcher.matches();
        }
        return matchVar;
    }

    @Override
    public String toString() {
        return "FindStrategyOnePatternLastInstance [oneMatcher=" + ((oneMatcher != null)?oneMatcher.pattern().pattern():("null"))
                + ", mainInstanceNumber=" + mainInstanceNumber
                + ", currentInstanceNumber=" + currentInstanceNumber
                + ", currResult=" + currResult + "]";
    }

}
