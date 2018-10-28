package org.eclipselabs.real.core.searchresult;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

import org.eclipselabs.real.core.searchobject.ISearchObjectDateInfo;
import org.eclipselabs.real.core.searchobject.crit.AcceptanceCriterionStage;
import org.eclipselabs.real.core.searchobject.crit.IAcceptanceCriterion;
import org.eclipselabs.real.core.searchobject.param.IReplaceableParam;
import org.eclipselabs.real.core.searchobject.param.ReplaceableParamKey;
import org.eclipselabs.real.core.searchresult.resultobject.ISearchResultObject;
import org.eclipselabs.real.core.searchresult.sort.IInternalSortRequest;

/**
 * This is the highest level abstraction of a search result.
 * The search result is a collection of search result objects (SROs).
 * The SROs can be
 * - sorted (with sort requests {@link IInternalSortRequest})
 * - merged with other search results from the same search object (SRO arrays merge)
 *
 * The search result also contains many (some cloned) properties of the originating
 * search object (some acceptance criteria, the date info).
 *
 * Also a very important property for the search result is the replace table
 * with which is was created. The search object contains only the templates
 * of regular expressions. These are turned into real regular expressions by
 * replacing parameters with actual values. But the regexes themselves are not changed,
 * the search result simply stores the values of the parameters and therefore
 * can use them for further work.
 *
 * @author Vadim Korkin
 *
 * @param <O> the type of the search result object
 */
public interface ISearchResult<O extends ISearchResultObject> extends Cloneable {
    public List<O> getSRObjects();
    public void setSRObjects(List<O> srObjects);
    public void addSRObject(O newSRO);
    public void addAll(Collection<O> newSRO);

    public void sort();
    public void sort(IInternalSortRequest mergeReq);

    public List<IAcceptanceCriterion> getAcceptanceList();
    public void setAcceptanceList(List<IAcceptanceCriterion> acceptanceList);
    public List<IAcceptanceCriterion> getAcceptanceList(Predicate<Set<AcceptanceCriterionStage>> st);

    public List<IInternalSortRequest> getAllSortRequests();
    public void setAllSortRequests(List<IInternalSortRequest> allReq);
    public List<IInternalSortRequest> getSortRequests(Predicate<IInternalSortRequest> predicate);
    public void removeSortRequests(Predicate<IInternalSortRequest> predicate);

    public Map<String, String> getCachedReplaceTable();
    public void setCachedReplaceTable(Map<String, String> cachedReplaceTable);
    public void putToReplaceTable(Map<String, String> cachedReplaceTable);
    public void putToReplaceTable(String newKey, String newValue);

    public Map<ReplaceableParamKey, IReplaceableParam<?>> getCachedReplaceParams();
    public void setCachedReplaceParams(Map<ReplaceableParamKey, IReplaceableParam<?>> replaceParams);
    public Map<ReplaceableParamKey, IReplaceableParam<?>> getAllCachedReplaceParams();
    public void setAllCachedReplaceParams(Map<ReplaceableParamKey, IReplaceableParam<?>> replaceParams);

    public void setReplaceTables(Map<String, String> cachedReplaceTable, Map<ReplaceableParamKey, IReplaceableParam<?>> replParams,
            Map<ReplaceableParamKey, IReplaceableParam<?>> allReplParams);

    public String getSearchObjectName();
    public Integer getRegexFlags();
    public void setRegexFlags(Integer newFlags);

    public List<ISearchObjectDateInfo> getDateInfos();
    public void setDateInfos(List<ISearchObjectDateInfo> dateInfo);

    public List<String> getTextList();
    public String getTextConcat();
    public Long getTextConcatLength();
    public ISearchResult<O> getInstance();

    public <M extends ISearchResult<X>, X extends O> void merge(List<M> mergeList);
    public <M extends ISearchResult<X>, X extends O> void merge(List<M> mergeList, IInternalSortRequest mergeComp);

    public <M extends ISearchResult<O>> void merge(M mergeObj);

    public <M extends ISearchResult<O>> void merge(M mergeObj, IInternalSortRequest mergeComp);
    public void mergeSRO(List<? extends O> sroList, IInternalSortRequest mergeReq);

    /**
     * Sometimes the search result may contain a mix of result objects with correct years
     * and result objects with year 1970 (it means the log didn't contain a year).
     * In order to avoid scanning the full list again during search every year present
     * in the result objects is added to the Set foundYears. The sets are merged when the result objects merge.
     * @return all years present in this search result (in result objects)
     */
    public Set<Integer> getFoundYears();

    /**
     * Sometimes the search result may contain a mix of result objects with correct years
     * and result objects with year 1970 (it means the log didn't contain a year).
     * In order to avoid scanning the full list again during search every year present
     * in the result objects is added to this set. The sets are merged when the result objects merge.
     * @param foundYears new foundYears set
     */
    public void setFoundYears(Set<Integer> foundYears);
    /**
     * If the result objects contain a mix of correct years (from log entries
     * that contain the year) and 1970 this method will try to find for every 1970 result object
     * a result object with the correct year (not 1970) and closest month/day hours:minutes:secods:milliseconds.
     * Then the year 1970 is replaced with the year from the found closest result object.
     */
    public void guessYears();

    /**
     * This method as expected returns an independent copy of this object
     * @return an independent copy of this object
     * @throws CloneNotSupportedException inherited from {@code Object.clone()}
     */
    public ISearchResult<O> clone() throws CloneNotSupportedException;
}
