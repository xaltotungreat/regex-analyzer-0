package org.eclipselabs.real.core.searchobject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipselabs.real.core.logfile.LogFileTypeKey;
import org.eclipselabs.real.core.searchobject.param.IReplaceableParam;
import org.eclipselabs.real.core.searchobject.param.ReplaceableParamKey;
import org.eclipselabs.real.core.searchresult.IKeyedComplexSearchResult;
import org.eclipselabs.real.core.searchresult.IKeyedSearchResult;
import org.eclipselabs.real.core.searchresult.ISearchResult;
import org.eclipselabs.real.core.searchresult.resultobject.IComplexSearchResultObject;
import org.eclipselabs.real.core.searchresult.resultobject.ISearchResultObject;

public abstract class KeyedComplexSearchObjectImpl<R extends IKeyedComplexSearchResult<O,W,X,Q>,
            O extends IComplexSearchResultObject<W,X,Q>,
            V extends ISearchObject<W,X>,W extends ISearchResult<X>, X extends ISearchResultObject,Q>
        extends ComplexSearchObjectImpl<R, O, V, W, X, Q> implements IKeyedComplexSearchObject<R, O, V, W, X, Q> {
    //private static final Logger log = LogManager.getLogger(KeyedComplexSearchObjectImpl.class);

    protected IKeyedSO keyedSODelegate = new KeyedSOImpl(this);

    public KeyedComplexSearchObjectImpl(SearchObjectType aType, String aName) {
        super(aType, aName);
    }

    @Override
    public LogFileTypeKey getLogFileType() {
        return keyedSODelegate.getLogFileType();
    }

    @Override
    public Map<String, String> getFinalReplaceTable(Map<ReplaceableParamKey, IReplaceableParam<?>> customReplaceParams, Map<String,String> fineTuneReplaceTable) {
        return keyedSODelegate.getFinalReplaceTable(customReplaceParams, fineTuneReplaceTable);
    }

    @Override
    public Map<String, String> getParentReplaceTable(Map<String, String> customReplaceTable) {
        return keyedSODelegate.getParentReplaceTable(customReplaceTable);
    }

    @Override
    public Map<ReplaceableParamKey, IReplaceableParam<?>> getAllReplaceParams(Map<ReplaceableParamKey, IReplaceableParam<?>> customReplaceParams) {
        return keyedSODelegate.getAllReplaceParams(customReplaceParams);
    }

    @Override
    public Map<ReplaceableParamKey, IReplaceableParam<?>> getParentReplaceParams(Map<ReplaceableParamKey, IReplaceableParam<?>> customReplaceParams) {
        return keyedSODelegate.getParentReplaceParams(customReplaceParams);
    }

    @Override
    public ISearchObjectGroup<String> getSearchObjectGroup() {
        return keyedSODelegate.getSearchObjectGroup();
    }

    @Override
    public ISearchObjectGroup<String> getCloneSearchObjectGroup() {
        ISearchObjectGroup<String> res = null;
        try {
            res = keyedSODelegate.getSearchObjectGroup().clone();
        } catch (CloneNotSupportedException e) {
        }
        return res;
    }

    @Override
    public void setSearchObjectGroup(ISearchObjectGroup<String> newGroup) {
        keyedSODelegate.setSearchObjectGroup(newGroup);
    }

    @Override
    public Map<String, String> getSearchObjectTags() {
        return keyedSODelegate.getSearchObjectTags();
    }

    @Override
    public void setSearchObjectTags(Map<String, String> soTags) {
        keyedSODelegate.setSearchObjectTags(soTags);
    }

    @Override
    public Set<LogFileTypeKey> getRequiredLogTypes() {
        return keyedSODelegate.getRequiredLogTypes();
    }

    @Override
    public void setRequiredLogTypes(Set<LogFileTypeKey> requiredLogFileTypes) {
        keyedSODelegate.setRequiredLogTypes(requiredLogFileTypes);
    }

    @Override
    public List<ISearchObjectDateInfo> getDateInfos() {
        return keyedSODelegate.getDateInfos();
    }

    @Override
    public void setDateInfos( List<ISearchObjectDateInfo> newDateInfo) {
        keyedSODelegate.setDateInfos(newDateInfo);
    }

    @Override
    public IKeyedSearchObject<? extends IKeyedSearchResult<?>, ? extends ISearchResultObject> getParent() {
        return keyedSODelegate.getParent();
    }

    @Override
    public void setParent(IKeyedSearchObject<? extends IKeyedSearchResult<?>, ? extends ISearchResultObject> newParent) {
        keyedSODelegate.setParent(newParent);
    }

    @Override
    public IKeyedComplexSearchObject<R, O, V, W, X, Q> clone() throws CloneNotSupportedException {
        KeyedComplexSearchObjectImpl<R, O, V, W, X, Q> cloneObj = (KeyedComplexSearchObjectImpl<R, O, V, W, X, Q>)super.clone();
        IKeyedSO cloneSODelegate = new KeyedSOImpl(cloneObj);
        if (keyedSODelegate.getSearchObjectGroup() != null) {
            cloneSODelegate.setSearchObjectGroup(keyedSODelegate.getSearchObjectGroup().clone());
        }
        if (keyedSODelegate.getSearchObjectTags() != null) {
            Map<String,String> newTags = new ConcurrentHashMap<>();
            newTags.putAll(keyedSODelegate.getSearchObjectTags());
            cloneSODelegate.setSearchObjectTags(newTags);
        }
        if (keyedSODelegate.getRequiredLogTypes() != null) {
            Set<LogFileTypeKey> newReqLFT = new HashSet<>();
            for (LogFileTypeKey lftKey : getRequiredLogTypes()) {
                newReqLFT.add(lftKey.clone());
            }
            cloneSODelegate.setRequiredLogTypes(newReqLFT);
        }
        if (keyedSODelegate.getDateInfos() != null) {
            List<ISearchObjectDateInfo> newInfos = new ArrayList<>();
            for (ISearchObjectDateInfo di : getDateInfos()) {
                newInfos.add(di.clone());
            }
        }
        cloneObj.keyedSODelegate = cloneSODelegate;
        return cloneObj;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((theType == null) ? 0 : theType.hashCode());
        result = prime * result + ((searchObjectName == null) ? 0 : searchObjectName.hashCode());
        result = prime * result + ((getSearchObjectGroup() == null) ? 0 : getSearchObjectGroup().hashCode());
        result = prime * result + ((getSearchObjectTags() == null) ? 0 : getSearchObjectTags().hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        KeyedComplexSearchObjectImpl<? extends IKeyedComplexSearchResult<?,?,?,?>,
                ? extends IComplexSearchResultObject<?,?,?>,
                ? extends ISearchObject<?,?>,? extends ISearchResult<?>, ? extends ISearchResultObject,?>
            other = (KeyedComplexSearchObjectImpl<? extends IKeyedComplexSearchResult<?,?,?,?>,
                    ? extends IComplexSearchResultObject<?,?,?>,
                            ? extends ISearchObject<?,?>,? extends ISearchResult<?>, ? extends ISearchResultObject,?>) obj;
        if (theType != other.theType) {
            return false;
        }
        if (searchObjectName == null) {
            if (other.searchObjectName != null) {
                return false;
            }
        } else if (!searchObjectName.equals(other.searchObjectName)) {
            return false;
        }
        if (getSearchObjectGroup() == null) {
            if (other.getSearchObjectGroup() != null) {
                return false;
            }
        } else if (!getSearchObjectGroup().equals(other.getSearchObjectGroup())) {
            return false;
        }
        if (getSearchObjectTags() == null) {
            if (other.getSearchObjectTags() != null) {
                return false;
            }
        } else if (!getSearchObjectTags().equals(other.getSearchObjectTags())) {
            return false;
        }
        return true;
    }

}
