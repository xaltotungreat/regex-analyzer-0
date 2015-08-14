package org.eclipselabs.real.core.searchresult;

import java.util.List;
import java.util.Map;

import org.eclipselabs.real.core.searchobject.ISearchObjectGroup;
import org.eclipselabs.real.core.searchobject.param.IReplaceableParam;
import org.eclipselabs.real.core.searchobject.param.ReplaceableParamKey;
import org.eclipselabs.real.core.searchresult.resultobject.ISRORegex;
import org.eclipselabs.real.core.searchresult.sort.IInternalSortRequest;

public class SRRegex extends KeyedSearchResultImpl<ISRORegex> implements ISRRegex {

    public SRRegex(String aSOKey, List<IInternalSortRequest> aSortRequestList, 
            Map<String,String> replaceTable, ISearchObjectGroup<String> aGroup, Map<String,String> soTags) {
        this(aSOKey, aSortRequestList, replaceTable, null, null, aGroup, soTags);
    }
    
    public SRRegex(String aSOName, List<IInternalSortRequest> aSortRequestList,  
            Map<String,String> aReplaceTable, Map<ReplaceableParamKey, IReplaceableParam<?>> customParams, Map<ReplaceableParamKey, IReplaceableParam<?>> allParams, 
            ISearchObjectGroup<String> aGroup, Map<String,String> tagsMap) {
        super(aSOName, aSortRequestList, aReplaceTable, customParams, allParams, aGroup, tagsMap);
    }
    
    public SRRegex(String aSOName, List<IInternalSortRequest> aSortRequestList, List<ISRORegex> initSRList) {
        super(aSOName, aSortRequestList, initSRList);
    }

    public SRRegex(String aSOName) {
        super(aSOName);
    }

    
    @Override
    public ISearchResult<ISRORegex> getInstance() {
        return new SRRegex(this.getSearchObjectName(), this.getAllSortRequests(), this.getSRObjects());
    }

}
