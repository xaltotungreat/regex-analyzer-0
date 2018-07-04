package org.eclipselabs.real.core.config.ml.regex;

import java.util.List;
import java.util.Map;

import org.eclipselabs.real.core.searchobject.IKeyedSearchObject;
import org.eclipselabs.real.core.searchobject.SearchObjectKey;
import org.eclipselabs.real.core.searchobject.param.IReplaceableParam;
import org.eclipselabs.real.core.searchobject.param.ReplaceableParamKey;
import org.eclipselabs.real.core.searchobject.ref.IRefKeyedSOContainer;
import org.eclipselabs.real.core.searchobject.ref.RefKeyedSO;
import org.eclipselabs.real.core.searchresult.IKeyedSearchResult;
import org.eclipselabs.real.core.searchresult.resultobject.ISearchResultObject;

public interface IRegexConfigCompletionCallback {
    public void addSearchObject(SearchObjectKey soKey, 
            IKeyedSearchObject<? extends IKeyedSearchResult<?>,? extends ISearchResultObject> param);
    public void addReplaceableParam(ReplaceableParamKey rpKey, IReplaceableParam<?> param);
    
    public void addAllSearchObject(Map<SearchObjectKey, 
            IKeyedSearchObject<? extends IKeyedSearchResult<?>,? extends ISearchResultObject>> searchObjMap);
    public void addAllReplaceableParam(Map<ReplaceableParamKey, IReplaceableParam<?>> replaceParamMap);
    
    public void resolveAllRefs(List<RefKeyedSO<? extends IKeyedSearchObject<? extends IKeyedSearchResult<?>,? extends ISearchResultObject>>> refsList,
            List<IRefKeyedSOContainer> refContainersList);
}
