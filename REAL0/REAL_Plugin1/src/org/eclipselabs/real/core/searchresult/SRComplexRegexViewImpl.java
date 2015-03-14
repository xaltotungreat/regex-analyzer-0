package org.eclipselabs.real.core.searchresult;

import java.util.List;
import java.util.Map;

import org.eclipselabs.real.core.searchresult.resultobject.ISROComplexRegexView;

public class SRComplexRegexViewImpl extends SearchResultImpl<ISROComplexRegexView> implements
        ISRComplexRegexView {

    public SRComplexRegexViewImpl(String aSOName) {
        super(aSOName);
    }

    public SRComplexRegexViewImpl(String aSOName,
            List<ISROComplexRegexView> initSRList) {
        super(aSOName, null, null, initSRList);
    }
    
    public SRComplexRegexViewImpl(String aSOName, Map<String,String> aReplaceTable, List<ISROComplexRegexView> initSRList) {
        super(aSOName, null, aReplaceTable, initSRList);
    }

    @Override
    public ISearchResult<ISROComplexRegexView> getInstance() {
        return new SRComplexRegexViewImpl(this.getSearchObjectName(), this.getSRObjects());
    }


}
