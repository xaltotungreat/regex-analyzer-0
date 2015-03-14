package org.eclipselabs.real.core.searchresult.resultobject;

import java.util.Calendar;

public class SRORegexImpl extends SearchResultObjectImpl implements ISRORegex {

    public SRORegexImpl(String aText, Integer aStartPos, Integer aEndPos) {
        super(aText, aStartPos, aEndPos);
    }
    
    public SRORegexImpl(String aText, Integer aStartPos, Integer aEndPos, Calendar dt) {
        super(aText, aStartPos, aEndPos, dt);
    }

}
