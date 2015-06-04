package org.eclipselabs.real.core.regex;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipselabs.real.core.util.FindTextResult;

class FindStrategyOnePatternInstance extends FindStrategyInstanceImpl {

    protected Matcher oneMatcher;

    public FindStrategyOnePatternInstance(Pattern pt, String text, Integer resNum) {
        super(FindStrategyType.ONE_PATTERN_INSTANCE, text, resNum);
        if (pt != null) {
            oneMatcher = pt.matcher(text);
        }
    }

    @Override
    public boolean find() {
        boolean booleanResult = false;
        while (oneMatcher.find()) {
            if (mainInstanceNumber != null) {
                if (currentInstanceNumber == mainInstanceNumber) {
                    booleanResult = true;
                    currResult = new FindTextResult(oneMatcher.group(), oneMatcher.start(), oneMatcher.end());
                    currentInstanceNumber++;
                    break;
                }
                currentInstanceNumber++;
            } else {
                booleanResult = true;
                currResult = new FindTextResult(oneMatcher.group(), oneMatcher.start(), oneMatcher.end());
                currentInstanceNumber++;
                break;
            }
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
        return "FindStrategyOnePatternInstance [oneMatcher=" + ((oneMatcher != null)?oneMatcher.pattern().pattern():("null"))
                + ", mainInstanceNumber=" + mainInstanceNumber
                + ", currentInstanceNumber=" + currentInstanceNumber
                + ", currResult=" + currResult + "]";
    }

}
