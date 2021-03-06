package org.eclipselabs.real.core.searchresult;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipselabs.real.core.searchobject.ISearchObjectGroup;
import org.eclipselabs.real.core.searchobject.param.IReplaceableParam;
import org.eclipselabs.real.core.searchobject.param.ReplaceableParamKey;
import org.eclipselabs.real.core.searchresult.resultobject.IComplexSearchResultObject;
import org.eclipselabs.real.core.searchresult.resultobject.ISearchResultObject;
import org.eclipselabs.real.core.searchresult.sort.IInternalSortRequest;

public abstract class KeyedComplexSearchResultImpl<O extends IComplexSearchResultObject<W,X,Q>,
                W extends ISearchResult<X>, X extends ISearchResultObject,Q> 
        extends ComplexSearchResultImpl<O, W, X, Q> implements IKeyedComplexSearchResult<O, W, X, Q> {

    protected ISearchObjectGroup<String> soGroup;
    protected Map<String,String> soTags;
    
    public KeyedComplexSearchResultImpl(String aSOName) {
        super(aSOName);
    }
    
    public KeyedComplexSearchResultImpl(String aSOName, List<IInternalSortRequest> aSortRequestList) {
        super(aSOName, aSortRequestList);
    }
    
    public KeyedComplexSearchResultImpl(String aSOName, List<IInternalSortRequest> aSortRequestList, List<O> initSRList) {
        super(aSOName, aSortRequestList, initSRList);
    }
    
    public KeyedComplexSearchResultImpl(String aSOName, List<IInternalSortRequest> aSortRequestList, 
            Map<String,String> aCachedReplaceTable) {
        super(aSOName, aSortRequestList, aCachedReplaceTable);
    }
    
    public KeyedComplexSearchResultImpl(String aSOName, List<IInternalSortRequest> aSortRequestList, 
            Map<String,String> aReplaceTable, ISearchObjectGroup<String> aGroup, Map<String,String> tagsMap) {
        this(aSOName, aSortRequestList, aReplaceTable, null, null, aGroup, tagsMap);
    }
    
    public KeyedComplexSearchResultImpl(String aSOName, List<IInternalSortRequest> aSortRequestList,  
            Map<String,String> aReplaceTable, Map<ReplaceableParamKey, IReplaceableParam<?>> customParams, Map<ReplaceableParamKey, IReplaceableParam<?>> allParams, 
            ISearchObjectGroup<String> aGroup, Map<String,String> tagsMap) {
        super(aSOName, aSortRequestList, aReplaceTable, customParams, allParams);
        soGroup = aGroup;
        soTags = new HashMap<String, String>(tagsMap);
    }
    
    public KeyedComplexSearchResultImpl(String aSOName, List<IInternalSortRequest> aSortRequestList, 
            Map<String,String> aCachedReplaceTable, List<O> initSRList) {
        super(aSOName, aSortRequestList, aCachedReplaceTable, initSRList);
    }
    
    public KeyedComplexSearchResultImpl(IKeyedComplexSearchResult<O,W,X,Q> copyObj) {
        super((IComplexSearchResult<O,W,X,Q>)copyObj);
        soGroup = copyObj.getSearchObjectGroup();
        soTags = new HashMap<String,String>(copyObj.getSearchObjectTags());
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
