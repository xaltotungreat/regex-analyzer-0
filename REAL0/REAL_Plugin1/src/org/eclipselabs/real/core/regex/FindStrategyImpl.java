package org.eclipselabs.real.core.regex;

import org.eclipselabs.real.core.util.FindTextResult;

abstract class FindStrategyImpl implements IMatcherWrapper {

    //private static final Logger log = LogManager.getLogger(FindStrategyImpl.class);
    protected FindStrategyType type;
    protected int regionStart;
    protected int regionEnd;
    protected FindTextResult currResult;
    
    protected int endText;
    
    public FindStrategyImpl(FindStrategyType aType, String text) {
        type = aType;
        regionStart = 0;
        regionEnd = text.length();
        endText = text.length();
    }
    
    @Override
    public FindTextResult getResult() {
        return currResult;
    }

    public void region(int rgStart, int rgEnd) {
        regionStart = rgStart;
        regionEnd = rgEnd;
    }

    @Override
    public FindStrategyType getType() {
        return type;
    }

}
