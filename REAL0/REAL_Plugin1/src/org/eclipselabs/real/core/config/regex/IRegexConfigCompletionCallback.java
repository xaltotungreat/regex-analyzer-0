package org.eclipselabs.real.core.config.regex;

import java.util.List;
import java.util.Map;

import org.eclipselabs.real.core.searchobject.IKeyedSearchObject;
import org.eclipselabs.real.core.searchobject.SearchObjectKey;
import org.eclipselabs.real.core.searchobject.param.IReplaceParam;
import org.eclipselabs.real.core.searchobject.param.ReplaceParamKey;
import org.eclipselabs.real.core.searchobject.ref.IRefKeyedSOContainer;
import org.eclipselabs.real.core.searchobject.ref.RefKeyedSO;
import org.eclipselabs.real.core.searchresult.IKeyedSearchResult;
import org.eclipselabs.real.core.searchresult.resultobject.ISearchResultObject;

public interface IRegexConfigCompletionCallback {
    public void addSearchObject(SearchObjectKey soKey, 
            IKeyedSearchObject<? extends IKeyedSearchResult<?>,? extends ISearchResultObject> param);
    public void addReplaceParam(ReplaceParamKey rpKey, IReplaceParam<?> param);
    
    public void addAllSearchObject(Map<SearchObjectKey, 
            IKeyedSearchObject<? extends IKeyedSearchResult<?>,? extends ISearchResultObject>> searchObjMap);
    public void addAllReplaceParam(Map<ReplaceParamKey, IReplaceParam<?>> replaceParamMap);
    
    public void resolveAllRefs(List<RefKeyedSO<? extends IKeyedSearchObject<? extends IKeyedSearchResult<?>,? extends ISearchResultObject>>> refsList,
            List<IRefKeyedSOContainer> refContainersList);
}
