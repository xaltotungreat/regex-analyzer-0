package org.eclipselabs.real.core.searchobject.ref;

import java.util.List;

import org.eclipselabs.real.core.searchobject.IKeyedSearchObject;
import org.eclipselabs.real.core.searchobject.ISearchObjectRepository;
import org.eclipselabs.real.core.searchresult.IKeyedSearchResult;
import org.eclipselabs.real.core.searchresult.resultobject.ISearchResultObject;

/**
 * This interface contains all the operations of a ref container.
 * A ref container is a search object that contains independent references
 * that can be resolved against a search object repository.
 * Some search objects may include refs for resolving later (at the loading configuration phase).
 * In this case they have to implement this interface.
 *
 * @author Vadim Korkin
 *
 */
public interface IRefKeyedSOContainer {

    public List<RefKeyedSO<? extends IKeyedSearchObject<? extends IKeyedSearchResult<?>,? extends ISearchResultObject>>> getRefList();
    public void setRefList(List<RefKeyedSO<? extends IKeyedSearchObject<? extends IKeyedSearchResult<?>,? extends ISearchResultObject>>> newRefList);

    public <F extends RefKeyedSO<? extends IKeyedSearchObject<? extends IKeyedSearchResult<?>, ? extends ISearchResultObject>>> void addRef(F newRef);

    public boolean isContainRefs();

    /**
     * This method takes references in this container and tries to resolve them one by one
     * If a ref is resolved it is removed from the internal ref array and passed back
     * in the returned List.
     * @return a list of complete objects without any refs
     */
    public List<? extends IKeyedSearchObject<? extends IKeyedSearchResult<?>,? extends ISearchResultObject>> resolveRefs(ISearchObjectRepository rep);

    /**
     *
     * @return true if all references in the array have been resolved
     */
    public boolean isAllRefResolved();

}
