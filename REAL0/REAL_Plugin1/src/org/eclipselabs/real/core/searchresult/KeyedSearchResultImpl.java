package org.eclipselabs.real.core.searchresult;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipselabs.real.core.searchobject.ISearchObjectGroup;
import org.eclipselabs.real.core.searchobject.param.IReplaceParam;
import org.eclipselabs.real.core.searchobject.param.ReplaceParamKey;
import org.eclipselabs.real.core.searchresult.resultobject.ISearchResultObject;
import org.eclipselabs.real.core.searchresult.sort.IInternalSortRequest;

public abstract class KeyedSearchResultImpl<O extends ISearchResultObject> extends SearchResultImpl<O> implements IKeyedSearchResult<O> {

    protected ISearchObjectGroup<String> soGroup;
    protected Map<String,String> soTags;
    
    public KeyedSearchResultImpl(String aSOName) {
        super(aSOName);
    }
    
    public KeyedSearchResultImpl(String aSOName, List<IInternalSortRequest> aSortRequestList) {
        super(aSOName, aSortRequestList);
    }
    
    public KeyedSearchResultImpl(String aSOName, List<IInternalSortRequest> aSortRequestList, List<O> initSRList) {
        super(aSOName, aSortRequestList, initSRList);
    }
    
    public KeyedSearchResultImpl(String aSOName, List<IInternalSortRequest> aSortRequestList, 
            Map<String,String> aReplaceTable) {
        super(aSOName, aSortRequestList, aReplaceTable);
    }
    
    public KeyedSearchResultImpl(String aSOName, List<IInternalSortRequest> aSortRequestList, 
            Map<String,String> aReplaceTable, ISearchObjectGroup<String> aGroup, Map<String,String> tagsMap) {
        this(aSOName, aSortRequestList, aReplaceTable, null, null, aGroup, tagsMap);
    }
    
    public KeyedSearchResultImpl(String aSOName, List<IInternalSortRequest> aSortRequestList,  
            Map<String,String> aReplaceTable, Map<ReplaceParamKey, IReplaceParam<?>> customParams, Map<ReplaceParamKey, IReplaceParam<?>> allParams, 
            ISearchObjectGroup<String> aGroup, Map<String,String> tagsMap) {
        super(aSOName, aSortRequestList, aReplaceTable, customParams, allParams);
        soGroup = aGroup;
        soTags = new HashMap<String, String>(tagsMap);
    }
    
    public KeyedSearchResultImpl(KeyedSearchResultImpl<O> copyObj) {
        super((ISearchResult<O>)copyObj);
    }

    @Override
    public ISearchObjectGroup<String> getSearchObjectGroup() {
        return soGroup;
    }

    @Override
    public void setSearchObjectGroup(ISearchObjectGroup<String> newGroup) {
        soGroup = newGroup;
    }

    @Override
    public Map<String, String> getSearchObjectTags() {
        return soTags;
    }

    @Override
    public void setSearchObjectTags(Map<String, String> soTags) {
        this.soTags = soTags;
    }

    @Override
    public ISearchResult<O> clone() throws CloneNotSupportedException {
        KeyedSearchResultImpl<O> cloneObj = (KeyedSearchResultImpl<O>)super.clone();
        if (soTags != null) {
            Map<String,String> newTags = new HashMap<>();
            newTags.putAll(soTags);
            cloneObj.soTags = newTags;
        }
        return cloneObj;
    }

}
