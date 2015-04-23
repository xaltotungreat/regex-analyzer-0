package org.eclipselabs.real.core.searchresult;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipselabs.real.core.searchobject.ISearchObjectConstants;
import org.eclipselabs.real.core.searchobject.ISearchObjectDateInfo;
import org.eclipselabs.real.core.searchobject.SearchObjectUtil;
import org.eclipselabs.real.core.searchobject.crit.AcceptanceCriterionStage;
import org.eclipselabs.real.core.searchobject.crit.IAcceptanceCriterion;
import org.eclipselabs.real.core.searchobject.param.IReplaceParam;
import org.eclipselabs.real.core.searchobject.param.ReplaceParamKey;
import org.eclipselabs.real.core.searchresult.resultobject.ISearchResultObject;
import org.eclipselabs.real.core.searchresult.sort.IInternalSortRequest;
import org.eclipselabs.real.core.searchresult.sort.SortingApplicability;
import org.eclipselabs.real.core.util.IRealCoreConstants;

public abstract class SearchResultImpl<O extends ISearchResultObject> implements ISearchResult<O> {

    private static final Logger log = LogManager.getLogger(SearchResultImpl.class);
    private static final int MERGE_LOOKUP_LIMIT = 20;
    // the name of the search object
    protected String soName;
    // String replace table specifically for replacements in regexes
    protected Map<String,String> cachedReplaceTable;
    // the params for this SO specified by the user
    protected Map<ReplaceParamKey, IReplaceParam<?>> customCachedParams;
    // this list includes not only the params specified by the user but also
    // params from up the group hierarchy and up the SO hierarchy
    protected Map<ReplaceParamKey, IReplaceParam<?>> allCachedParams;
    // acceptance criteria for the merge phase
    protected List<IAcceptanceCriterion> acceptanceList = Collections.synchronizedList(new ArrayList<IAcceptanceCriterion>());
    // cloned sort requests from the search object
    protected List<IInternalSortRequest> sortRequestList = Collections.synchronizedList(new ArrayList<IInternalSortRequest>());
    // if regex flag have been specified for this search in the search object
    // they will be copied to this field
    protected Integer regexFlags;
    // cloned date info from the search object
    protected ISearchObjectDateInfo dateInfo;
    // this set is used in search results merging to guess the correct year
    // (mostly in search scripts) if for some result objects the year info is resent
    // and for some is not
    protected Set<Integer> foundYears = Collections.synchronizedSet(new HashSet<Integer>());
    // The main list of search result objects
    protected List<O> srObjectsList = Collections.synchronizedList(new ArrayList<O>());

    public SearchResultImpl(String aSOName) {
        soName = aSOName;
    }

    public SearchResultImpl(String aSOName, List<IInternalSortRequest> aSortRequestList) {
        soName = aSOName;
        if (aSortRequestList != null) {
            sortRequestList.addAll(aSortRequestList);
        }
    }

    public SearchResultImpl(String aSOName, List<IInternalSortRequest> aSortRequestList, List<O> initSRList) {
        soName = aSOName;
        if (aSortRequestList != null) {
            sortRequestList.addAll(aSortRequestList);
        }
        srObjectsList = initSRList;
    }

    public SearchResultImpl(String aSOName, List<IInternalSortRequest> aSortRequestList, Map<String,String> aReplaceTable) {
        this(aSOName, aSortRequestList, aReplaceTable, null, null);
    }

    public SearchResultImpl(String aSOName, List<IInternalSortRequest> aSortRequestList, Map<String,String> aReplaceTable,
            Map<ReplaceParamKey, IReplaceParam<?>> customParams, Map<ReplaceParamKey, IReplaceParam<?>> allParams) {
        soName = aSOName;
        if (aSortRequestList != null) {
            sortRequestList.addAll(aSortRequestList);
        }
        cachedReplaceTable = aReplaceTable;
        customCachedParams = customParams;
        allCachedParams = allParams;
    }

    public SearchResultImpl(String aSOName, List<IInternalSortRequest> aSortRequestList, Map<String,String> aReplaceTable, List<O> initSRList) {
        soName = aSOName;
        if (aSortRequestList != null) {
            sortRequestList.addAll(aSortRequestList);
        }
        cachedReplaceTable = aReplaceTable;
        srObjectsList = initSRList;
    }

    public SearchResultImpl(ISearchResult<O> copyObj) {
        soName = copyObj.getSearchObjectName();
        if (copyObj.getAllSortRequests() != null) {
            sortRequestList.addAll(copyObj.getAllSortRequests());
        }
        cachedReplaceTable = new HashMap<String,String>(copyObj.getCachedReplaceTable());
        srObjectsList = new ArrayList<O>(copyObj.getSRObjects());
    }

    @Override
    public String getSearchObjectName() {
        return soName;
    }

    @Override
    public <M extends ISearchResult<X>, X extends O> void merge(List<M> mergeList) {
        if ((sortRequestList != null) && (!sortRequestList.isEmpty())) {
            merge(mergeList, sortRequestList.get(0));
        } else {
            log.warn("merge no sort requests add to the end");
            merge(mergeList, (IInternalSortRequest)null);
        }
    }

    @Override
    public <M extends ISearchResult<X>, X extends O> void merge(List<M> mergeList, IInternalSortRequest mergeReq) {
        if ((mergeReq != null) && (mergeReq.getSortApplicability().getScope() >= SortingApplicability.MERGE_RESULTS.getScope())) {
            merge(mergeList, mergeReq.getComparator(this));
        } else {
            log.warn("merge Passed internal sort request " + mergeReq + " doesn't have " + SortingApplicability.MERGE_RESULTS
                    + " in the scope or is null. No intelligent merge performed");
            merge(mergeList, (Comparator<O>) null);
        }
    }

    @Override
    public <M extends ISearchResult<O>> void merge(M mergeObj) {
        if ((sortRequestList != null) && (!sortRequestList.isEmpty())) {
            merge(mergeObj, sortRequestList.get(0));
        } else {
            log.warn("merge no sort requests add to the end");
            merge(mergeObj, (IInternalSortRequest)null);
        }
    }

    @Override
    public <M extends ISearchResult<O>> void merge(M mergeObj, IInternalSortRequest mergeReq) {
        if (mergeObj != null) {
            if ((mergeReq != null) && (mergeReq.getSortApplicability().getScope() >= SortingApplicability.MERGE_RESULTS.getScope())) {
                merge(mergeObj, mergeReq.getComparator(this));
            } else {
                log.warn("merge Passed internal sort request " + mergeReq + " doesn't have " + SortingApplicability.MERGE_RESULTS
                        + " in the scope or is null. No intelligent merge performed");
                merge(mergeObj, (Comparator<O>) null);
            }
        } else {
            log.warn("merge null mergeObj");
        }
    }

    protected <M extends ISearchResult<X>, X extends O> void merge(List<M> mergeList, final Comparator<O> mergeComp) {
        if (srObjectsList == null) {
            srObjectsList = Collections.synchronizedList(new ArrayList<O>());
        }
        if ((mergeList != null) && (!mergeList.isEmpty())) {
            for (M currMergeObj : mergeList) {
                if ((currMergeObj.getSRObjects() != null) && (!currMergeObj.getSRObjects().isEmpty())) {
                    guessYears(currMergeObj);
                    mergeSRO(currMergeObj.getSRObjects(), mergeComp);
                }
            }
        } else {
            log.warn("merge merge list is null or empty " + mergeList);
        }
    }

    @Override
    public void mergeSRO(List<? extends O> sroList, IInternalSortRequest mergeReq) {
        if ((mergeReq != null) && (mergeReq.getSortApplicability().getScope() >= SortingApplicability.MERGE_RESULTS.getScope())) {
            mergeSRO(sroList, mergeReq.getComparator(this));
        } else {
            log.warn("merge Passed internal sort request " + mergeReq + " doesn't have " + SortingApplicability.MERGE_RESULTS
                    + " in the scope or is null. No intelligent merge performed");
            mergeSRO(sroList, (Comparator<O>) null);
        }
    }

    protected void mergeSRO(List<? extends O> sroList, Comparator<O> comp) {
        if ((sroList != null) && (!sroList.isEmpty())) {
            List<O> sroToMerge = getAcceptedMergeList(sroList);
            int positionToInsert = srObjectsList.size();
            if ((comp != null) && (!srObjectsList.isEmpty())) {
                O thisFirstSRO = srObjectsList.get(0);
                O otherFirstSRO = sroToMerge.get(0);
                O thisLastSRO = srObjectsList.get(srObjectsList.size() - 1);
                O otherLastSRO = sroToMerge.get(sroToMerge.size() - 1);

                /* the last SRO in this list may have time that is actually after the first SRO in the other list.
                 * Timing may not be strict in the log, different timestamps may mix
                 * need to make sure we look up at least some records in this list to find one that is before the other list
                 * Example
                 *
                 * This list              Other list
                 * 2020/10/20 15:20
                 * ...
                 * 2020/10/20 15:35
                 * 2020/10/20 15:36       2020/10/20 15:36
                 * 2020/10/20 15:37       2020/10/20 15:37
                 *                        ...
                 *                        2020/10/20 15:50
                 *
                 * When merging we need to find this record 2020/10/20 15:36 in this list that has the timestamp equal
                 * to the first SRO in the other list
                 */
                if ((srObjectsList.size() >= sroToMerge.size()) && (comp.compare(otherFirstSRO, thisFirstSRO) > 0)
                        && (SearchResultUtil.getByPredicate(srObjectsList, (O thislp) -> comp.compare(otherFirstSRO, thislp) >= 0,
                                true, MERGE_LOOKUP_LIMIT)).isPresent()) {
                    positionToInsert = srObjectsList.size();
                } else if ((srObjectsList.size() < sroToMerge.size()) && (comp.compare(otherLastSRO, thisLastSRO) > 0)
                        && (SearchResultUtil.getByPredicate(sroToMerge, (O thislp) -> comp.compare(thisLastSRO, thislp) <= 0,
                        false, MERGE_LOOKUP_LIMIT)).isPresent()) {
                    positionToInsert = srObjectsList.size();
                } else if ((srObjectsList.size() >= sroToMerge.size()) && (comp.compare(otherLastSRO, thisLastSRO) < 0)
                        && (SearchResultUtil.getByPredicate(srObjectsList, (O thislp) -> comp.compare(otherLastSRO, thislp) <= 0,
                        false, MERGE_LOOKUP_LIMIT)).isPresent()) {
                    positionToInsert = 0;
                } else if ((srObjectsList.size() < sroToMerge.size()) && (comp.compare(otherFirstSRO, thisFirstSRO) < 0)
                        && (SearchResultUtil.getByPredicate(sroToMerge, (O thislp) -> comp.compare(thisFirstSRO, thislp) >= 0,
                                true, MERGE_LOOKUP_LIMIT)).isPresent()) {
                    positionToInsert = 0;
                } else if ((comp.compare(otherFirstSRO, thisFirstSRO) > 0) && (comp.compare(otherLastSRO, thisLastSRO) < 0)) {
                    /*
                     * In this case the other SROs are in the middle of already merged SROs
                     * the merge was out of order first merging the early part with the late part
                     * and only after that the part in the middle. Look up the best position (cannot guarantee
                     * exact the same position as in the file)
                     *
                     * Example
                     * This list              Other list
                     *
                     * 2020/10/20 15:20
                     * ...
                     * 2020/10/20 15:35
                     * 2020/10/20 15:36
                     *                        2020/10/20 15:37
                     * ...                    2020/10/20 15:38
                     * 2020/10/20 15:40
                     */
                    positionToInsert = 0;
                    O greaterSRO = thisFirstSRO;
                    while ((positionToInsert < srObjectsList.size()) && (comp.compare(otherFirstSRO, greaterSRO) >= 0)) {
                        positionToInsert++;
                        greaterSRO = srObjectsList.get(positionToInsert);
                    }
                } else if ((comp.compare(otherFirstSRO, thisFirstSRO) < 0) && (comp.compare(otherLastSRO, thisLastSRO) > 0)) {
                    log.error("mergeSRO The list being merged (other list) begins earlier than the already merged one and ends after the already merged one. IT CANNOT HAPPEN");
                } else if (comp.compare(otherFirstSRO, thisFirstSRO) == 0) {
                    /* if they are equal it means the SR with the smallest number of results
                     * must go first. A small number of results in one result then the first record of the other
                     * result with the same value
                     * Example
                     * This list              Other list
                     *                        2020/10/20 15:20
                     * 2020/10/20 15:20       2020/10/20 15:20
                     * ...
                     * 2020/10/20 15:35
                     * 2020/10/20 15:36
                     * 2020/10/20 15:37
                     *
                     */
                    if (srObjectsList.size() > sroToMerge.size()) {
                        positionToInsert = 0;
                    } else {
                        positionToInsert = srObjectsList.size();
                    }
                } else if (comp.compare(thisLastSRO, otherLastSRO) == 0) {
                    /* if they are equal it means the SR with the smallest number of results
                     * must go last. Some results in one result then the first record of the other result
                     * with the same value
                     * Example
                     * This list              Other list
                     *
                     * 2020/10/20 15:20
                     * ...
                     * 2020/10/20 15:35
                     * 2020/10/20 15:36
                     * 2020/10/20 15:37       2020/10/20 15:37
                     *                        2020/10/20 15:20
                     */
                    if (srObjectsList.size() > sroToMerge.size()) {
                        positionToInsert = srObjectsList.size();
                    } else {
                        positionToInsert = 0;
                    }
                }
            }
            srObjectsList.addAll(positionToInsert, sroToMerge);
        } else {
            log.warn("mergeSRO sro list to merge is null or empty");
        }
    }

    protected List<O> getAcceptedMergeList(List<? extends O> sroList) {
        List<O> sroToMerge = new ArrayList<>(sroList);
        List<IAcceptanceCriterion> mergeAC = getAcceptanceList(new Predicate<Set<AcceptanceCriterionStage>>() {

            @Override
            public boolean test(Set<AcceptanceCriterionStage> t) {
                return t.contains(AcceptanceCriterionStage.MERGE);
            }
        });
        if ((mergeAC != null) && (!mergeAC.isEmpty())) {
            sroToMerge.clear();
            for (O currObj : sroList) {
                if (SearchObjectUtil.accept(currObj, mergeAC, this)) {
                    sroToMerge.add(currObj);
                }
            }
        }
        return sroToMerge;
    }

    protected <M extends ISearchResult<X>, X extends O> void merge(M mergeObj, Comparator<O> comp) {
        if ((mergeObj != null) && (mergeObj.getSRObjects() != null)) {
            guessYears(mergeObj);
            mergeSRO(mergeObj.getSRObjects(), comp);
        }
    }

    protected <M extends ISearchResult<X>, X extends O> void guessYears(M otherSR) {
        if (foundYears == null) {
            foundYears = Collections.synchronizedSet(new HashSet<Integer>());
        }
        if (otherSR.getFoundYears() != null) {
            foundYears.addAll(otherSR.getFoundYears());
        }
        if ((foundYears.contains(ISearchObjectConstants.DEFAULT_NOT_FOUND_YEAR)) && (foundYears.size() > 1)) {
            List<O> correctYearList = new ArrayList<>();
            List<O> noYearList = new ArrayList<>();
            for (O sro : srObjectsList) {
                if (sro.getDate() != null) {
                    if (sro.getDate().get(Calendar.YEAR) > ISearchObjectConstants.DEFAULT_NOT_FOUND_YEAR) {
                        correctYearList.add(sro);
                    } else {
                        noYearList.add(sro);
                    }
                }
            }
            for (O sro : otherSR.getSRObjects()) {
                if (sro.getDate() != null) {
                    if (sro.getDate().get(Calendar.YEAR) > ISearchObjectConstants.DEFAULT_NOT_FOUND_YEAR) {
                        correctYearList.add(sro);
                    } else {
                        noYearList.add(sro);
                    }
                }
            }
            SimpleDateFormat fmt = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss.SSS", IRealCoreConstants.MAIN_DATE_LOCALE);
            for (O currSRO : noYearList) {
                final Calendar tmpCal = (Calendar)currSRO.getDate().clone();
                final long thisSROMillis = tmpCal.getTimeInMillis();
                Comparator<O> dateComp = new Comparator<O>() {

                    @Override
                    public int compare(O o1, O o2) {
                        int result = 0;
                        Calendar cal1 = null;
                        if (o1.getDate() != null) {
                            cal1 = (Calendar)o1.getDate().clone();
                        }
                        Calendar cal2 = null;
                        if (o2.getDate() != null) {
                            cal2 = (Calendar)o2.getDate().clone();
                        }
                        if ((cal1 != null) && (cal2 != null)) {
                            cal1.set(Calendar.YEAR, 1970);
                            cal2.set(Calendar.YEAR, 1970);
                            Long diff1 = Math.abs(thisSROMillis - cal1.getTimeInMillis());
                            Long diff2 = Math.abs(thisSROMillis - cal2.getTimeInMillis());
                            result = diff1.compareTo(diff2);
                        } else if ((cal1 == null) && (cal2 != null)) {
                            result = 1;
                        } else if ((cal1 != null) && (cal2 == null)) {
                            result = -1;
                        }
                        return result;
                    }
                };
                O minSRO = Collections.min(correctYearList, dateComp);
                log.debug("guessYears original date " + fmt.format(currSRO.getDate()) + " closest " + fmt.format(minSRO.getDate()));
                minSRO.getDate().set(Calendar.YEAR, minSRO.getDate().get(Calendar.YEAR));
            }
            foundYears.remove(ISearchObjectConstants.DEFAULT_NOT_FOUND_YEAR);
        }
    }

    @Override
    public void guessYears() {
        if ((foundYears != null) && (foundYears.contains(ISearchObjectConstants.DEFAULT_NOT_FOUND_YEAR)) && (foundYears.size() > 1)) {
            List<O> correctYearList = new ArrayList<>();
            List<O> noYearList = new ArrayList<>();
            for (O sro : srObjectsList) {
                if (sro.getDate() != null) {
                    if (sro.getDate().get(Calendar.YEAR) > ISearchObjectConstants.DEFAULT_NOT_FOUND_YEAR) {
                        correctYearList.add(sro);
                    } else {
                        noYearList.add(sro);
                    }
                }
            }
            SimpleDateFormat fmt = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss.SSS", IRealCoreConstants.MAIN_DATE_LOCALE);
            for (O currSRO : noYearList) {
                final Calendar tmpCal = (Calendar)currSRO.getDate().clone();
                final long thisSROMillis = tmpCal.getTimeInMillis();
                Comparator<O> dateComp = new Comparator<O>() {

                    @Override
                    public int compare(O o1, O o2) {
                        int result = 0;
                        Calendar cal1 = null;
                        if (o1.getDate() != null) {
                            cal1 = (Calendar)o1.getDate().clone();
                        }
                        Calendar cal2 = null;
                        if (o2.getDate() != null) {
                            cal2 = (Calendar)o2.getDate().clone();
                        }
                        if ((cal1 != null) && (cal2 != null)) {
                            cal1.set(Calendar.YEAR, 1970);
                            cal2.set(Calendar.YEAR, 1970);
                            Long diff1 = Math.abs(thisSROMillis - cal1.getTimeInMillis());
                            Long diff2 = Math.abs(thisSROMillis - cal2.getTimeInMillis());
                            result = diff1.compareTo(diff2);
                        } else if ((cal1 == null) && (cal2 != null)) {
                            result = 1;
                        } else if ((cal1 != null) && (cal2 == null)) {
                            result = -1;
                        }
                        return result;
                    }
                };
                O minSRO = Collections.min(correctYearList, dateComp);
                log.debug("guessYears original date " + fmt.format(currSRO.getDate().getTime()) + " closest " + fmt.format(minSRO.getDate().getTime()));
                currSRO.getDate().set(Calendar.YEAR, minSRO.getDate().get(Calendar.YEAR));
            }
            foundYears.remove(ISearchObjectConstants.DEFAULT_NOT_FOUND_YEAR);
        }
    }

    @Override
    public String getTextConcat() {
        StringBuilder sb = new StringBuilder();
        for (ISearchResultObject resObj : srObjectsList) {
            sb.append(resObj.getText());
        }
        return sb.toString();
    }

    @Override
    public Long getTextConcatLength() {
        long length = 0;
        for (ISearchResultObject resObj : srObjectsList) {
            length += resObj.getText().length();
        }
        return length;
    }

    @Override
    public List<String> getTextList() {
        List<String> res = new ArrayList<String>();
        for (ISearchResultObject resObj : srObjectsList) {
            res.add(resObj.getText());
        }
        return res;
    }

    @Override
    public void addAll(Collection<O> newSRO) {
        srObjectsList.addAll(newSRO);
    }

    @Override
    public void addSRObject(O newObject) {
        srObjectsList.add(newObject);
    }

    @Override
    public List<O> getSRObjects() {
        return srObjectsList;
    }

    @Override
    public void setSRObjects(List<O> srObjects) {
        this.srObjectsList = srObjects;
    }

    @Override
    public void sort() {
        if ((sortRequestList != null) && (!sortRequestList.isEmpty())) {
            sort(sortRequestList.get(0));
        } else {
            log.error("No sort requests available, unable to sort");
        }
    }

    @Override
    public void sort(IInternalSortRequest mergeReq) {
        if ((mergeReq != null) && (mergeReq.getSortApplicability() != null) && (mergeReq.getSortApplicability().getScope() >= SortingApplicability.ALL.getScope())) {
            Collections.sort(srObjectsList, sortRequestList.get(0).getComparator(this));
        } else {
            log.warn("Passed internal sort request" + mergeReq + " doesn't have "
                    + SortingApplicability.ALL + " in the scope. No sort performed");
        }
    }

    @Override
    public List<IAcceptanceCriterion> getAcceptanceList() {
        return acceptanceList;
    }

    @Override
    public void setAcceptanceList(List<IAcceptanceCriterion> acceptanceList) {
        this.acceptanceList = acceptanceList;
    }

    @Override
    public List<IAcceptanceCriterion> getAcceptanceList(Predicate<Set<AcceptanceCriterionStage>> st) {
        List<IAcceptanceCriterion> result = null;
        if (acceptanceList != null) {
            if (st != null) {
                result = new ArrayList<>();
                synchronized (acceptanceList) {
                    for (IAcceptanceCriterion ac : acceptanceList) {
                        if (st.test(ac.getStages())) {
                            result.add(ac);
                        }
                    }
                }
            } else {
                log.warn("getCloneAcceptanceList the stages is null");
            }
        } else {
            log.warn("getCloneAcceptanceList the original list is null");
        }
        return result;
    }

    @Override
    public List<IInternalSortRequest> getAllSortRequests() {
        return sortRequestList;
    }

    @Override
    public void setAllSortRequests(List<IInternalSortRequest> allReq) {
        sortRequestList.clear();
        if (allReq != null) {
            sortRequestList.addAll(allReq);
        }
    }

    @Override
    public List<IInternalSortRequest> getSortRequests(Predicate<IInternalSortRequest> predicate) {
        List<IInternalSortRequest> res = new ArrayList<>();
        if ((sortRequestList != null) && (!sortRequestList.isEmpty()) && (predicate != null)) {
            for (IInternalSortRequest currSortReq : sortRequestList) {
                if (predicate.test(currSortReq)) {
                    try {
                        res.add(currSortReq.clone());
                    } catch (CloneNotSupportedException e) {
                        log.error("Clone not supported",e);
                    }
                }
            }
        }
        return res;
    }

    @Override
    public void removeSortRequests(Predicate<IInternalSortRequest> predicate) {
        if ((sortRequestList != null) && (!sortRequestList.isEmpty()) && (predicate != null)) {
            for (IInternalSortRequest currSortReq : sortRequestList) {
                if (predicate.test(currSortReq)) {
                    sortRequestList.remove(currSortReq);
                }
            }
        } else {
            log.warn("removeSortRequests sortRequestList is null nothing to remove");
        }
    }

    @Override
    public Map<String, String> getCachedReplaceTable() {
        return cachedReplaceTable;
    }

    @Override
    public void setCachedReplaceTable(Map<String, String> cachedReplaceTable) {
        this.cachedReplaceTable = cachedReplaceTable;
    }

    @Override
    public void putToReplaceTable(Map<String, String> cachedReplaceTable) {
        if ((this.cachedReplaceTable != null) && (cachedReplaceTable != null)) {
            this.cachedReplaceTable.putAll(cachedReplaceTable);
        } else {
            log.error("One of the values is null this.cachedReplaceTable " + this.cachedReplaceTable
                    + " cachedReplaceTable " + cachedReplaceTable);
        }
    }

    @Override
    public void putToReplaceTable(String newKey, String newValue) {
        if ((cachedReplaceTable != null) && (newKey != null) && (newValue != null)) {
            cachedReplaceTable.put(newKey, newValue);
        } else {
            log.error("One of the values is null cachedReplaceTable " + cachedReplaceTable
                    + " newKey " + newKey + " newValue " + newValue);
        }
    }

    @Override
    public Map<ReplaceParamKey, IReplaceParam<?>> getCachedReplaceParams() {
        return customCachedParams;
    }

    @Override
    public void setCachedReplaceParams(Map<ReplaceParamKey, IReplaceParam<?>> replaceParams) {
        customCachedParams = replaceParams;
    }

    @Override
    public Map<ReplaceParamKey, IReplaceParam<?>> getAllCachedReplaceParams() {
        return allCachedParams;
    }

    @Override
    public void setAllCachedReplaceParams(Map<ReplaceParamKey, IReplaceParam<?>> replaceParams) {
        allCachedParams = replaceParams;
    }

    @Override
    public void setReplaceTables(Map<String, String> cachedReplaceTable, Map<ReplaceParamKey, IReplaceParam<?>> replParams, Map<ReplaceParamKey, IReplaceParam<?>> allReplParams) {
        this.cachedReplaceTable = cachedReplaceTable;
        customCachedParams = replParams;
        allCachedParams = allReplParams;
    }

    @Override
    public Integer getRegexFlags() {
        return regexFlags;
    }

    @Override
    public void setRegexFlags(Integer newFlags) {
        regexFlags = newFlags;
    }

    @Override
    public ISearchObjectDateInfo getDateInfo() {
        return dateInfo;
    }

    @Override
    public void setDateInfo(ISearchObjectDateInfo dateInfo) {
        this.dateInfo = dateInfo;
    }

    @Override
    public Set<Integer> getFoundYears() {
        return foundYears;
    }

    @Override
    public void setFoundYears(Set<Integer> foundYears) {
        this.foundYears = foundYears;
    }

    @Override
    public ISearchResult<O> clone() throws CloneNotSupportedException {
        SearchResultImpl<O> cloneObj = (SearchResultImpl<O>)super.clone();
        if (cachedReplaceTable != null) {
            Map<String,String> newReplaceTable = new HashMap<>();
            newReplaceTable.putAll(cachedReplaceTable);
            cloneObj.cachedReplaceTable = newReplaceTable;
        }
        if (sortRequestList != null) {
            List<IInternalSortRequest> newSortRequestList = Collections.synchronizedList(new ArrayList<IInternalSortRequest>());
            for (IInternalSortRequest currReq : sortRequestList) {
                newSortRequestList.add(currReq.clone());
            }
            cloneObj.sortRequestList = newSortRequestList;
        }
        if (srObjectsList != null) {
            List<O> newSRObjectsList = Collections.synchronizedList(new ArrayList<O>());
            for (O currSRObj : srObjectsList) {
                newSRObjectsList.add((O)currSRObj.clone());
            }
            cloneObj.srObjectsList = newSRObjectsList;
        }
        return cloneObj;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        int i = 1;
        for (O srObj : srObjectsList) {
            sb.append(i).append(" ").append(srObj);
            i++;
        }
        return sb.toString();
    }

}
