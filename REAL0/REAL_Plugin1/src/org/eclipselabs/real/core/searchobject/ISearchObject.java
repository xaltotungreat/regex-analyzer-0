package org.eclipselabs.real.core.searchobject;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import org.eclipselabs.real.core.searchobject.crit.IAcceptanceCriterion;
import org.eclipselabs.real.core.searchobject.param.IReplaceableParam;
import org.eclipselabs.real.core.searchobject.param.ReplaceableParamKey;
import org.eclipselabs.real.core.searchresult.ISearchResult;
import org.eclipselabs.real.core.searchresult.resultobject.ISearchResultObject;
import org.eclipselabs.real.core.searchresult.sort.IInternalSortRequest;
import org.eclipselabs.real.core.searchresult.sort.SortingType;
import org.eclipselabs.real.core.util.ITypedObject;

/**
 * This is the highest level abstraction of a search object.
 * It can be anything one would like to search for. But it is fairly safe to assume that it is
 * a number of regular expressions that are executed according to some algorithm to get the result.
 * @author Vadim Korkin
 *
 * @param <R> The type of the result. Must be a descendant of ISearchResult
 * @param <O> The type of one object of the result. Must be a descendant of ISearchResultObject
 */
public interface ISearchObject<R extends ISearchResult<O>,O extends ISearchResultObject>
        extends ITypedObject<SearchObjectType>, Cloneable {

    /**
     * At the highest level of abstraction the search object can only have a name and a description.
     * The name is just a readable string to identify this object. It doesn't have to be unique.
     * @return The name of the search object
     */
    public String getSearchObjectName();

    /**
     * At the highest level of abstraction the search object can only have a name and a description.
     * The name is just a readable string to identify this object. It doesn't have to be unique.
     * @param newName the new name for this search object.
     */
    public void setSearchObjectName(String newName);

    /**
     * At the highest level of abstraction the search object can only have a name and a description.
     * A description can describe what this search object does or provide some details.
     * It doesn't have to be unique.
     * @return The description of the search object
     */
    public String getSearchObjectDescription();

    /**
     * At the highest level of abstraction the search object can only have a name and a description.
     * A description can describe what this search object does or provide some details.
     * It doesn't have to be unique.
     * @param newDescription the new description for this search objects
     */
    public void setSearchObjectDescription(String newDescription);

    /**
     * Even though in general a search object
     * can be anything it is usually a number of regular expressions that are executed according to some algorithm.
     * The default regex flags can be set at creation
     * @return the regex flags for this search object
     */
    public Integer getRegexFlags();

    /**
     * Even though in general a search object
     * can be anything it is usually a number of regular expressions that are executed according to some algorithm.
     * @param newFlags the new regex flags. After the flags have been set all subsequent search operations
     * will be executed with these flags
     */
    public void setRegexFlags(Integer newFlags);

    /**
     * Perform the search with the parameters specified in the object {@link PerformSearchRequest}
     * @return The result of the search. Must be a descendant of ISearchResult
     */
    public R performSearch(PerformSearchRequest request);

    /**
     * Adds a replace parameter {@link IReplaceableParam} to this search object.
     * When a search is performed with this search object all params in regular expressions
     * are replaced with actual values.
     *
     * @param newParam the parameter to add
     */
    public void addParam(IReplaceableParam<?> newParam);

    /**
     * Returns the replace parameter if it exists. Here I used the new Optional
     * from Java 8 because the parameter may not exist for this key
     * @param key the key of the replace param
     * @return the replace param for this key
     */
    public Optional<IReplaceableParam<?>> getParam(ReplaceableParamKey key);

    /**
     * Returns true if the param exists false otherwise
     * @param key the key of the replace param
     * @return true if the param exists false otherwise
     */
    public boolean paramExists(ReplaceableParamKey key);

    /**
     * Removes the replace param from this search object
     * @param key the key of the replace param
     * @return true if at least one parameter was removed false otherwise
     */
    public boolean removeParam(ReplaceableParamKey key);

    /**
     * As a usual search object should include regular expressions it can also declare parameters.
     * A parameter for a search object is a string value that is replaced just before
     * the expression is executed to the value specified by the user. The values for all parameters declared
     * by the search object are usually set by the user. Also parameters can be used as constants
     * for assigning this constant only once then getting the value by replacing the constant.
     *
     * @return the list of parameters for this search object
     */
    //public List<IReplaceParam<?>> getParamList();

    /**
     * As a usual search object should include regular expressions it can also declare parameters.
     * A parameter for a search object is a string value that is replaced just before
     * the expression is executed to the value specified by the user. The values for all parameters declared
     * by the search object are usually set by the user. Also parameters can be used as constants
     * for assigning this constant only once then getting the value by replacing the constant.
     *
     * @return the list of clones of parameters for this search object
     */
    public List<IReplaceableParam<?>> getCloneParamList();

    /**
     * A search operation can be time consuming especially if many files need to be searched.
     * In this case the user has to be notified that some progress is being made.
     * This object is obtained before the search operation and passed to the performSearch method.
     * Some values of this object are updated within performSearch to allow for monitoring
     * search progress.
     * @return a new search monitor
     */
    public ISearchProgressMonitor getSearchProgressMonitor();

    /**
     * It is possible that one search object may have more than one sort request available
     * In this case the requests are kept in a list in which the the first sort request
     * is the most preferred the second is second preferred etc.
     * @return the list of sort requests available for this search object in the preferred order
     */
    public List<IInternalSortRequest> getSortRequestList();

    /**
     * It is possible that one search object may have more than one sort request available
     * In this case the requests are kept in a list in which the the first sort request
     * is the most preferred the second is second preferred etc.
     * @param sortRequestList the list of sort requests for this search object in the preferred order
     */
    public void setSortRequestList(List<IInternalSortRequest> sortRequestList);

    /**
     * Sometimes the requirements for selecting objects may be quite complex
     * In these cases even if it may be possible to create a regular expression
     * that will select objects in accordance with the requirements this expression
     * will be very complex and will very likely hang on the Java regex engine.
     * It makes sense to separate the complex requirements into several regular expressions.
     * The first regular expression selects a lot of objects then the filters from
     * the acceptance list select the right ones.
     *
     * The interface IAcceptanceCriterion resembles Predicate<T> but in the test method
     * two parameters are passed: the search result object that is going to be tested
     * and the search result. It is assumed that for many search objects the search result
     * plays the context role during the search.
     *
     * @return the list of acceptance criteria
     */
    public List<IAcceptanceCriterion> getAcceptanceList();

    /**
     * Sometimes the requirements for selecting objects may be quite complex
     * In these cases even if it may be possible to create a regular expression
     * that will select objects in accordance with the requirements this expression
     * will be very complex and will very likely hang on the Java regex engine.
     * It makes sense to separate the complex requirements into several regular expressions.
     * The first regular expression selects a lot of objects then the filters from
     * the acceptance list select the right ones.
     *
     * The interface IAcceptanceCriterion resembles Predicate<T> but in the test method
     * two parameters are passed: the search result object that is going to be tested
     * and the search result. It is assumed that for many search objects the search result
     * plays the context role during the search.
     *
     * @param stagePred the Predicate to filter the criteria
     * @return the list of acceptance criteria that satisfy the predicate
     */
    public List<IAcceptanceCriterion> getAcceptanceList(Predicate<IAcceptanceCriterion> stagePred);

    /**
     * Sometimes the requirements for selecting objects may be quite complex
     * In these cases even if it may be possible to create a regular expression
     * that will select objects in accordance with the requirements this expression
     * will be very complex and will very likely hang on the Java regex engine.
     * It makes sense to separate the complex requirements into several regular expressions.
     * The first regular expression selects a lot of objects then the filters from
     * the acceptance list select the right ones.
     *
     * The interface IAcceptanceCriterion resembles Predicate<T> but in the test method
     * two parameters are passed: the search result object that is going to be tested
     * and the search result. It is assumed that for many search objects the search result
     * plays the context role during the search.
     *
     * The set method is used when it is necessary to update the reference
     * If you need to add more than one acceptance criteria you should use
     * List.addAll
     * @param acceptanceList the new reference to the acceptance list
     */
    public void setAcceptanceList(List<IAcceptanceCriterion> acceptanceList);

    /**
     * This method returns clones of all acceptance criteria for the specified stages
     * @param st the stages for which acceptance criteria need to be cloned and returned
     * @return a list of clones of acceptance criteria for all specified stages
     */
    public List<IAcceptanceCriterion> getCloneAcceptanceList(Predicate<IAcceptanceCriterion> stagePred);

    /**
     * For example it is very convenient to sort the results by time. The REAL framework allows this
     * by providing the sort type and the date info. The date info contains information
     * how to extract the date from the result text and the sort type defines if this information
     * should be used. It is DATE_TIME sorting type
     * Also the REAL framework allows to sort requests by the natural ordering of some text
     * that is extracted from the search result text by a regular expression. It is REGEX sorting type
     * @param requestedType the sorting type to check for availability
     * @return true if a internal sort request with the sorting type "requestedType"
     * is available for this search object false otherwise
     */
    public boolean isSortingAvailable(SortingType requestedType);

    /**
     * Cloning as expected returns an independent copy of this object.
     * @return an independent copy of this object.
     * @throws CloneNotSupportedException this exception is inherited from {@code Object.clone}
     */
    public ISearchObject<R,O> clone() throws CloneNotSupportedException;

}
