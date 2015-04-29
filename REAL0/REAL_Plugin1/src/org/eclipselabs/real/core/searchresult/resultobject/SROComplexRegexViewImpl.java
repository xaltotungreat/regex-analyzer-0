package org.eclipselabs.real.core.searchresult.resultobject;

import java.time.LocalDateTime;

public class SROComplexRegexViewImpl extends SearchResultObjectImpl implements ISROComplexRegexView {

    public SROComplexRegexViewImpl(String aText, Integer aStartPos, Integer aEndPos) {
        super(aText, aStartPos, aEndPos);
    }

    public SROComplexRegexViewImpl(String aText, Integer aStartPos, Integer aEndPos, LocalDateTime dt) {
        super(aText, aStartPos, aEndPos, dt);
    }

}
