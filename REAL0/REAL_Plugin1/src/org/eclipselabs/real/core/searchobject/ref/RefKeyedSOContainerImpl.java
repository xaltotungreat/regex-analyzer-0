package org.eclipselabs.real.core.searchobject.ref;

import java.util.ArrayList;
import java.util.List;

import org.eclipselabs.real.core.searchobject.IKeyedSearchObject;
import org.eclipselabs.real.core.searchobject.ISearchObjectRepository;
import org.eclipselabs.real.core.searchresult.IKeyedSearchResult;
import org.eclipselabs.real.core.searchresult.resultobject.ISearchResultObject;

/**
 * A convenience implementation of a RefKeyedSO container
 * @author Vadim Korkin
 *
 */
public class RefKeyedSOContainerImpl implements IRefKeyedSOContainer {

    protected List<RefKeyedSO<? extends IKeyedSearchObject<? extends IKeyedSearchResult<?>, ? extends ISearchResultObject>>> refList
            = new ArrayList<>();

    @Override
    public List<RefKeyedSO<? extends IKeyedSearchObject<? extends IKeyedSearchResult<?>, ? extends ISearchResultObject>>> getRefList() {
        return refList;
    }

    @Override
    public void setRefList(List<RefKeyedSO<? extends IKeyedSearchObject<? extends IKeyedSearchResult<?>, ? extends ISearchResultObject>>> newRefList) {
        refList = newRefList;
    }

    @Override
    public <F extends RefKeyedSO<? extends IKeyedSearchObject<? extends IKeyedSearchResult<?>, ? extends ISearchResultObject>>> 
            void addRef(F newRef) {
        if (refList != null) {
            refList.add(newRef);
        }
    }
    
    @Override
    public List<? extends IKeyedSearchObject<? extends IKeyedSearchResult<?>, ? extends ISearchResultObject>> resolveRefs(ISearchObjectRepository rep) {
        List<IKeyedSearchObject<? extends IKeyedSearchResult<?>, ? extends ISearchResultObject>> resultList = null;
        if ((refList != null) && (!refList.isEmpty())) {
            resultList = new ArrayList<>();
            List<RefKeyedSO<? extends IKeyedSearchObject<? extends IKeyedSearchResult<?>, ? extends ISearchResultObject>>> resolvedRefs = new ArrayList<>();
            for (RefKeyedSO<? extends IKeyedSearchObject<? extends IKeyedSearchResult<?>, ? extends ISearchResultObject>> currRef : refList) {
                IKeyedSearchObject<? extends IKeyedSearchResult<?>, ? extends ISearchResultObject> result = currRef.resolve(rep);
                if (result != null) {
                    resultList.add(result);
                    //refList.remove(currRef);
                    resolvedRefs.add(currRef);
                }
            }
            refList.removeAll(resolvedRefs);
        }
        if ((resultList != null) && (!resultList.isEmpty())) {
            return resultList;
        } else {
            return null;
        }
    }

    @Override
    public boolean isAllRefResolved() {
        return ((refList == null) || (refList.isEmpty()));
    }

    @Override
    public boolean isContainRefs() {
        return ((refList != null) && (!refList.isEmpty()));
    }

    @Override
    public String toString() {
        return "RefKeyedSOContainerImpl [refList=" + refList + "]";
    }


}
