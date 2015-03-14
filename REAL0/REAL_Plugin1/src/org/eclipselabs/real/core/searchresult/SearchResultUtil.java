package org.eclipselabs.real.core.searchresult;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipselabs.real.core.searchresult.resultobject.ISearchResultObject;
import org.eclipselabs.real.core.searchresult.sort.IInternalSortRequest;

public class SearchResultUtil {

    private static final Logger log = LogManager.getLogger(SearchResultUtil.class);

    private SearchResultUtil() {}

    public static <M extends ISearchResult<X>, X extends ISearchResultObject> M merge(List<M> mergeList, IInternalSortRequest mergeReq) {
        M mergedObj = null;
        if ((mergeList != null) && (!mergeList.isEmpty())) {
            M tmpMergeObj = mergeList.get(0);
            if (mergeReq != null) {
                final Comparator<X> resOrderComp = mergeReq.getComparator(mergeList.get(0));
                /* sort the results according to the first records
                 * this is necessary to avoid the scenario when some result
                 * is merged in the middle of already merged SROs. In this case cannot guarantee
                 * the exact order as in the files.
                 */
                Comparator<M> sortResults = new Comparator<M>() {

                    @Override
                    public int compare(M o1, M o2) {
                        int result = 0;
                        if ((!o1.getSRObjects().isEmpty()) && (!o2.getSRObjects().isEmpty())) {
                            X o1FirstSRO = o1.getSRObjects().get(0);
                            X o2FirstSRO = o2.getSRObjects().get(0);
                            result = resOrderComp.compare(o1FirstSRO, o2FirstSRO);
                        } else if ((!o1.getSRObjects().isEmpty()) && (o2.getSRObjects().isEmpty())) {
                            result = 1;
                        } else if ((o1.getSRObjects().isEmpty()) && (!o2.getSRObjects().isEmpty())) {
                            result = -1;
                        }
                        return result;
                    }
                };
                Collections.sort(mergeList, sortResults);
                mergedObj = mergeList.get(0);
                mergedObj.guessYears();
                mergeList.remove(0);
                mergedObj.merge(mergeList, mergeReq);
            } else if ((tmpMergeObj.getAllSortRequests() != null) && !tmpMergeObj.getAllSortRequests().isEmpty()) {
                merge(mergeList, tmpMergeObj.getAllSortRequests().get(0));
            }
        }
        return mergedObj;
    }

    public static <M extends ISearchResult<X>, X extends ISearchResultObject> M merge(List<M> mergeList) {
        if ((mergeList != null) && (!mergeList.isEmpty())) {
            M tmpMergeObj = mergeList.get(0);
            if ((tmpMergeObj.getAllSortRequests() != null) && !tmpMergeObj.getAllSortRequests().isEmpty()) {
                return merge(mergeList, tmpMergeObj.getAllSortRequests().get(0));
            }
            log.error("merge no merge requests returning null");
        }
        return null;
    }

    /**
     * Returns Optional containing the first matching element in the list
     * The test is performed with the supplied predicate
     * @param lst the List to search
     * @param pred the predicate to test the elements
     * @param backward the direction (forward/backward)
     * @param limit the amount of elements to test
     * @return Optional containing the first matching predicate if it exists or an empty Optional
     */
    public static <T> Optional<T> getByPredicate(List<T> lst, Predicate<T> pred, boolean backward, int limit) {
        Optional<T> result = Optional.empty();
        if (limit > lst.size()) {
            limit = lst.size();
        }
        if (!backward) {
            int i = 0;
            while (i < limit) {
                if (pred.test(lst.get(i))) {
                    result = Optional.of(lst.get(i));
                    break;
                }
                i++;
            }
        } else {
            int i = lst.size() - 1;
            while (i >= lst.size() - limit) {
                if (pred.test(lst.get(i))) {
                    result = Optional.of(lst.get(i));
                    break;
                }
                i--;
            }
        }
        return result;
    }
}
