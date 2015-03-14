package org.eclipselabs.real.core.searchresult;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipselabs.real.core.searchobject.ISearchObjectDateInfo;
import org.eclipselabs.real.core.searchobject.crit.AcceptanceCriterionStage;
import org.eclipselabs.real.core.searchobject.crit.IAcceptanceCriterion;
import org.eclipselabs.real.core.searchobject.param.IReplaceParam;
import org.eclipselabs.real.core.searchobject.param.ReplaceParamKey;
import org.eclipselabs.real.core.searchresult.resultobject.ISearchResultObject;
import org.eclipselabs.real.core.searchresult.sort.IInternalSortRequest;
import org.eclipselabs.real.core.util.RealPredicate;

public interface ISearchResult<O extends ISearchResultObject> extends Cloneable {
    public List<O> getSRObjects();
    public void setSRObjects(List<O> srObjects);
    public void addSRObject(O newSRO);
    public void addAll(Collection<O> newSRO);
    
    public void sort();
    public void sort(IInternalSortRequest mergeReq);
    
    public List<IAcceptanceCriterion> getAcceptanceList();
    public void setAcceptanceList(List<IAcceptanceCriterion> acceptanceList);
    public List<IAcceptanceCriterion> getAcceptanceList(RealPredicate<Set<AcceptanceCriterionStage>> st);

    public List<IInternalSortRequest> getAllSortRequests();
    public void setAllSortRequests(List<IInternalSortRequest> allReq);
    public List<IInternalSortRequest> getSortRequests(RealPredicate<IInternalSortRequest> predicate);
    public void removeSortRequests(RealPredicate<IInternalSortRequest> predicate);
    
    public Map<String, String> getCachedReplaceTable();
    public void setCachedReplaceTable(Map<String, String> cachedReplaceTable);
    public void putToReplaceTable(Map<String, String> cachedReplaceTable);
    public void putToReplaceTable(String newKey, String newValue);
    
    public Map<ReplaceParamKey, IReplaceParam<?>> getCachedReplaceParams();
    public void setCachedReplaceParams(Map<ReplaceParamKey, IReplaceParam<?>> replaceParams);
    public Map<ReplaceParamKey, IReplaceParam<?>> getAllCachedReplaceParams();
    public void setAllCachedReplaceParams(Map<ReplaceParamKey, IReplaceParam<?>> replaceParams);
    
    public void setReplaceTables(Map<String, String> cachedReplaceTable, Map<ReplaceParamKey, IReplaceParam<?>> replParams, 
            Map<ReplaceParamKey, IReplaceParam<?>> allReplParams);
    
    public String getSearchObjectName();
    public Integer getRegexFlags();
    public void setRegexFlags(Integer newFlags);
    
    public ISearchObjectDateInfo getDateInfo();
    public void setDateInfo(ISearchObjectDateInfo dateInfo);
    
    public List<String> getTextList();
    public String getTextConcat();
    public Long getTextConcatLength();
    public ISearchResult<O> getInstance();
    
    public <M extends ISearchResult<X>, X extends O> void merge(List<M> mergeList);
    public <M extends ISearchResult<X>, X extends O> void merge(List<M> mergeList, IInternalSortRequest mergeComp);
    //public <M extends ISearchResult<X>, X extends O> void merge(M mergeObj);
    public <M extends ISearchResult<O>> void merge(M mergeObj);
    //public <M extends ISearchResult<X>, X extends O> void merge(M mergeObj, IInternalSortRequest mergeComp);
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
