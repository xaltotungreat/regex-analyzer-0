package org.eclipselabs.real.core.searchobject.ref;

import java.util.List;

import org.eclipselabs.real.core.searchobject.IKeyedSearchObject;
import org.eclipselabs.real.core.searchobject.ISearchObjectRepository;
import org.eclipselabs.real.core.searchresult.IKeyedSearchResult;
import org.eclipselabs.real.core.searchresult.resultobject.ISearchResultObject;

/**
 * This interface contains all the operations of a ref container.
 * A ref container is a search object that contains independent references (to keyed search objects)
 * that can be resolved against a search object repository.
 * Some search objects may include refs for resolving later (at the loading configuration phase).
 * In this case they have to implement this interface.
 *
 * @author Vadim Korkin
 *
 */
public interface IRefKeyedSOContainer {

    /**
     * Returns the ref list of this container
     * @return the ref list of this container
     */
    public List<RefKeyedSO<? extends IKeyedSearchObject<? extends IKeyedSearchResult<?>,? extends ISearchResultObject>>> getRefList();
    /**
     * Sets the ref list for this container
     * @param newRefList the new ref list
     */
    public void setRefList(List<RefKeyedSO<? extends IKeyedSearchObject<? extends IKeyedSearchResult<?>,? extends ISearchResultObject>>> newRefList);

    /**
     * Adds a ref to a keyed SO to this container
     * @param newRef the new ref to add
     */
    public <F extends RefKeyedSO<? extends IKeyedSearchObject<? extends IKeyedSearchResult<?>, ? extends ISearchResultObject>>> void addRef(F newRef);

    /**
     * Returns true if this contains contains unresolved refs.
     * @return true if this contains contains unresolved refs false otherwise
     */
    public boolean isContainRefs();

    /**
     * This method takes references in this container and tries to resolve them one by one against the passed in repository
     * If a ref is resolved it is removed from the internal ref array and passed back in the returned List.
     * @return a list of complete objects without any refs
     */
    public List<? extends IKeyedSearchObject<? extends IKeyedSearchResult<?>,? extends ISearchResultObject>> resolveRefs(ISearchObjectRepository rep);

    /**
     *
     * @return true if all references in the array have been resolved
     */
    public boolean isAllRefResolved();

}
