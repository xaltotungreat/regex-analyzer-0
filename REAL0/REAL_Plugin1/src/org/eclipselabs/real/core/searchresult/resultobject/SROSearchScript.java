package org.eclipselabs.real.core.searchresult.resultobject;

import java.util.Calendar;
import java.util.Map;

import org.eclipselabs.real.core.searchresult.ISRComplexRegexView;

public class SROSearchScript extends ComplexSearchResultObjectImpl<ISRComplexRegexView,ISROComplexRegexView,String> implements ISROSearchScript {

    public SROSearchScript(String logExcerpt, Integer aStartPos, Integer aEndPos) {
        super(logExcerpt, aStartPos, aEndPos);
    }
    
    public SROSearchScript(String logExcerpt, Integer aStartPos, Integer aEndPos, Calendar dt) {
        super(logExcerpt, aStartPos, aEndPos, dt);
    }
    
    public SROSearchScript(String logExcerpt, Map<String,ISRComplexRegexView> newViewMap,
            Integer aStartPos, Integer aEndPos) {
        super(logExcerpt, aStartPos, aEndPos);
        viewMap = newViewMap;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("LogRecord\n\t\t").append(text).append("\n");
        for (String viewKey : viewMap.keySet()) {
            sb.append("\tView name=").append(viewKey).append(" value=").append(viewMap.get(viewKey)).append("\n");
        }
        return sb.toString();
    }

}
