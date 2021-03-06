package org.eclipselabs.real.core.searchresult;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipselabs.real.core.searchobject.ISearchObjectGroup;
import org.eclipselabs.real.core.searchobject.param.IReplaceableParam;
import org.eclipselabs.real.core.searchobject.param.ReplaceableParamKey;
import org.eclipselabs.real.core.searchresult.resultobject.ISROComplexRegex;
import org.eclipselabs.real.core.searchresult.resultobject.ISROComplexRegexView;
import org.eclipselabs.real.core.searchresult.sort.IInternalSortRequest;

public class SRComplexRegexImpl extends KeyedComplexSearchResultImpl<ISROComplexRegex, 
        ISRComplexRegexView, ISROComplexRegexView, String> implements ISRComplexRegex {
    
    public SRComplexRegexImpl(String srName, List<IInternalSortRequest> aSortRequestList, Map<String,String> replaceTable) {
        super(srName, aSortRequestList, replaceTable);
    }
    
    public SRComplexRegexImpl(String aSOKey, List<IInternalSortRequest> aSortRequestList, 
            Map<String,String> replaceTable, ISearchObjectGroup<String> aGroup, Map<String,String> soTags) {
        this(aSOKey, aSortRequestList, replaceTable, null, null, aGroup, soTags);
    }
    
    public SRComplexRegexImpl(String aSOName, List<IInternalSortRequest> aSortRequestList,  
            Map<String,String> aReplaceTable, Map<ReplaceableParamKey, IReplaceableParam<?>> customParams, Map<ReplaceableParamKey, IReplaceableParam<?>> allParams, 
            ISearchObjectGroup<String> aGroup, Map<String,String> tagsMap) {
        super(aSOName, aSortRequestList, aReplaceTable, customParams, allParams, aGroup, tagsMap);
    }
    
    public SRComplexRegexImpl(ISRComplexRegex copyObj) {
        super(copyObj);
    }

    public SRComplexRegexImpl(String aSOKey, List<IInternalSortRequest> aSortRequestList,
            Map<String,String> replaceTable, List<ISROComplexRegex> initSRList) {
        super(aSOKey, aSortRequestList, replaceTable, initSRList);
    }
    
    @Override
    public ISearchResult<ISROComplexRegex> getInstance() {
        return new SRComplexRegexImpl(this);
    }

    @Override
    public ISearchResult<ISROComplexRegex> clone() throws CloneNotSupportedException {
        SRComplexRegexImpl cloneObj = (SRComplexRegexImpl)super.clone();
        if (viewOrder != null) {
            List<String> newViewOrder = new ArrayList<>();
            newViewOrder.addAll(viewOrder);
            cloneObj.viewOrder = newViewOrder;
        }
        return cloneObj;
    }

}
