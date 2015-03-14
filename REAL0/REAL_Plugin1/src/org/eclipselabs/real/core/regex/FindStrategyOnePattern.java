package org.eclipselabs.real.core.regex;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipselabs.real.core.util.FindTextResult;

class FindStrategyOnePattern extends FindStrategyImpl {

    protected Matcher oneMatcher;

    public FindStrategyOnePattern(Pattern pt, String text) {
        super(FindStrategyType.ONE_PATTERN, text);
        if (pt != null) {
            oneMatcher = pt.matcher(text);
        }
    }
    
    public FindStrategyOnePattern(FindStrategyType type, Pattern pt, String text) {
        super(type, text);
        if (pt != null) {
            oneMatcher = pt.matcher(text);
        }
    }


    @Override
    public void region(int rgStart, int rgEnd) {
        super.region(rgStart, rgEnd);
        if (oneMatcher != null) {
            oneMatcher.region(rgStart, rgEnd);
        }
    }


    @Override
    public boolean find() {
        boolean mainResult = false;
        if (oneMatcher != null) {
            mainResult = oneMatcher.find();
            if (mainResult) {
                currResult = new FindTextResult(oneMatcher.group(), oneMatcher.start(), oneMatcher.end());
            }
        }
        return mainResult;
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
        return "FindStrategyOnePattern [oneMatcher=" + ((oneMatcher != null)?oneMatcher.pattern().pattern():("null"))
                + ", currResult=" + currResult + "]";
    }



}
