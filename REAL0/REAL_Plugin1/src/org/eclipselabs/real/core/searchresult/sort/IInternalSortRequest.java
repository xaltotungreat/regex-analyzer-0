package org.eclipselabs.real.core.searchresult.sort;

import java.util.Comparator;
import org.eclipselabs.real.core.searchresult.ISearchResult;
import org.eclipselabs.real.core.searchresult.resultobject.ISearchResultObject;
import org.eclipselabs.real.core.util.ITypedObject;

/**
 * This interface is an abstraction of a sort request. A sort request contains the parameters
 * to perform a merge of the search results and sort them.
 * The sort request must implement the main method getComparator to return a comparator
 * for a search result object. 
 * Also the sort request has two main attributes: SortingType and SortingApplicability
 * SortingType reflects how sorting is performed (by a date by a string)
 * SortingApplicability reflects when sorting is performed: never, when merging search results
 * always (sort not only results from files but individual results as well) or any other options
 * 
 * Sorting during a merge operation means that only two search result objects 
 * from two search results are compared (the first or the last the exact algorithm is not guaranteed).
 * All search result objects from the search result with the lesser first/last search result object
 * is placed first in the resulting array of search result objects the other search result objects
 * come after in the list.
 * 
 * Example of comparison.
 * 
 * Search result 1 (SR1)                Search result 2 (SR2)
 *   SRO 1 - date 1 December              SRO 3 - date 3 December
 *   SRO 2 - date 2 December              SRo 4 - date 4 December
 *   
 *   The first SROs are compared:
 *   SRO 1 < SRO 3
 *   
 *   Resulting search result:
 *     SRO 1
 *     SRO 2
 *     SRO 3
 *     SRO 4
 * @author Vadim Korkin
 *
 */
public interface IInternalSortRequest extends ITypedObject<SortingType>,Cloneable {

    /**
     * The main method for a internal sort request. The returned comparator is used to 
     * sort an array of search result objects
     * @param searchRes the search result is sometimes used to obtain the necessary generics
     * parameters
     * @return a comparator to sort an array of search result objects
     */
    public <R extends ISearchResult<O>,O extends ISearchResultObject> Comparator<O> getComparator(R searchRes);
    
    /**
     * This method sorts the List passed in as a paarmeter
     * @param srtList the list to sort
     */
    public <R extends ISearchResult<O>, O extends ISearchResultObject> void sortResultObjects(R searchRes);
    
    /**
     * Just a name to discern one internal sort request from another
     * Currently this name doesn't serve any significant purpose 
     * @return a name of the sort request
     */
    public String getName();
    
    /**
     * Sets the name of the sort request
     * Currently this name doesn't serve any significant purpose 
     * @param name the name of the request
     */
    public void setName(String name);
    
    /**
     * Currently the internal sort request supports no sorting, during merge only
     * and ALL. 
     * @return the sorting applicability (or scope) for this internal sort request
     */
    public SortingApplicability getSortApplicability();
    
    /**
     * Sets the sorting applicability to this internal sort request
     * @param sortApplicability the new sorting applicability
     */
    public void setSortApplicability(SortingApplicability sortApplicability);
    
    /**
     * The internal sort requests may have install parameters 
     * i.e. some parameters from a search object may be modified before sorting is
     * executed. This means the internal sort request must be cloneable to avoid
     * modifying the original list of sort requests for the search object
     * @return a deep copy of this internal sort request
     * @throws CloneNotSupportedException
     */
    public IInternalSortRequest clone() throws CloneNotSupportedException;
}
