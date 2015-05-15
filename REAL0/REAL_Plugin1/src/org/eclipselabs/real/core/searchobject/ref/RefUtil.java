package org.eclipselabs.real.core.searchobject.ref;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipselabs.real.core.regex.IRealRegex;
import org.eclipselabs.real.core.searchobject.IKeyedSearchObject;
import org.eclipselabs.real.core.searchobject.SearchObjectController;
import org.eclipselabs.real.core.searchresult.IKeyedSearchResult;
import org.eclipselabs.real.core.searchresult.resultobject.ISearchResultObject;

/**
 * This class contains static utility methods for resolving and working with refs.
 *
 * @author Vadim Korkin
 *
 */
public class RefUtil {

    private static final Logger log = LogManager.getLogger(RefUtil.class);

    private RefUtil() {}

    public static <T> Predicate<T> getAlwaysTruePredicate() {
        return new Predicate<T>() {

            @Override
            public boolean test(T t) {
                return true;
            }
        };
    }

    public static <T> Predicate<T> getAlwaysFalsePredicate() {
        return new Predicate<T>() {

            @Override
            public boolean test(T t) {
                return false;
            }
        };
    }

    public static List<IKeyedSearchObject<? extends IKeyedSearchResult<?>,? extends ISearchResultObject>> getMatchingSO(RefKeyedSO<?> ref) {
        return SearchObjectController.INSTANCE.getSearchObjectRepository().getValues(ref.getSOKeyMatchPredicate());
    }

    public static boolean matchRefRealRegex(List<IRealRegex> originalList, List<RefRealRegex> refList) {
        boolean matches = true;
        if ((refList != null) && (!refList.isEmpty())) {
            for (RefRealRegex refRealRegex : refList) {
                if (RefType.MATCH.equals(refRealRegex.getType())) {
                    if (originalList == null) {
                        log.warn("matchByParameters real regex list is null, ref not matched (no more matching attempted)\n" + refRealRegex);
                        matches = false;
                        break;
                    }
                    boolean refMatches = false;
                    if (refRealRegex.getPosition() == null) {
                        if ((refRealRegex.getPosition() >= 0) && (refRealRegex.getPosition() < originalList.size())) {
                            refMatches = refRealRegex.defaultMatch(originalList.get(refRealRegex.getPosition().intValue()));
                        }
                    } else {
                        synchronized (originalList) {
                            for (int i = 0; i < originalList.size(); i++) {
                                IRealRegex currObjRegex = originalList.get(i);
                                if (refRealRegex.defaultMatch(currObjRegex)) {
                                    refMatches = refRealRegex.matchByParameters(currObjRegex);
                                    if (refMatches) {
                                        break;
                                    }
                                }
                            }
                        }
                    }
                    if (!refMatches) {
                        matches = false;
                        log.debug("matchByParameters RefRealRegex not matched " + refRealRegex);
                        break;
                    }
                }
            }
        }
        return matches;
    }

    /**
     * If the position is specified in this regex then the value is added to the specified position
     * Otherwise to the "end" of the List
     * @param originalList the reference to the List of IRealRegex in the object being resolved
     * @param refList the list of refs to IRealRegex to resolve
     * @return the number of objects added i.e. the number of refs to IRealRegex with the type ADD
     * successfully processed in this method
     */
    public static Integer addRefRealRegex(List<IRealRegex> originalList, List<RefRealRegex> refList) {
        Integer count = 0;
        if ((refList != null) && (!refList.isEmpty())) {
            for (RefRealRegex refRealRegex : refList) {
                if (RefType.ADD.equals(refRealRegex.getType())) {
                    if (refRealRegex.getValue() == null) {
                        log.error("addRefRealRegex null value for ref(cannot process this param)\n" + refRealRegex);
                        continue;
                    }
                    if (originalList == null) {
                        log.warn("addRefRealRegex real regex list is null, ref not matched (no params added)\n" + refRealRegex);
                        break;
                    }
                    if (refRealRegex.getPosition() != null) {
                        if ((refRealRegex.getPosition() >= 0) && (refRealRegex.getPosition() <= originalList.size())) {
                            originalList.add(refRealRegex.getPosition(), refRealRegex.getValue());
                            count++;
                        } else {
                            log.warn("addRefRealRegex Incorrect sort request position refname=" + refRealRegex.getName()
                                    + " position " + refRealRegex.getPosition() + " size=" + originalList.size());
                            Optional<IRealRegex> optRegex = originalList.stream().
                                    filter(reg -> refRealRegex.defaultMatch(reg)).findFirst();
                            if (!optRegex.isPresent()) {
                                originalList.add(refRealRegex.getValue());
                                count++;
                            } else {
                                log.warn("addRefRealRegex incorrect position and default match found (not added) refname=" + refRealRegex.getName());
                            }
                        }
                    } else {
                        Optional<IRealRegex> optRegex = originalList.stream().
                                filter(reg -> refRealRegex.defaultMatch(reg)).findFirst();
                        if (!optRegex.isPresent()) {
                            originalList.add(refRealRegex.getValue());
                            count++;
                        } else {
                            log.warn("addRefRealRegex default match found (not added) refname=" + refRealRegex.getName());
                        }
                    }
                }
            }
        }
        return count;
    }

    public static Integer replaceAddRefRealRegex(List<IRealRegex> originalList, List<RefRealRegex> refList) {
        Integer count = 0;
        if ((refList != null) && (!refList.isEmpty())) {
            for (RefRealRegex refRealRegex : refList) {
                if (RefType.REPLACE_ADD.equals(refRealRegex.getType())) {
                    if (refRealRegex.getValue() == null) {
                        log.error("replaceAddRefRealRegex null value for ref(cannot process this param)\n" + refRealRegex);
                        continue;
                    }
                    if (originalList == null) {
                        log.warn("replaceAddRefRealRegex real regex list is null, ref not replaced/added (no more refs replaced/added)\n" + refRealRegex);
                        break;
                    }
                    if (refRealRegex.getPosition() != null) {
                        if ((refRealRegex.getPosition() >= 0) && (refRealRegex.getPosition() < originalList.size())) {
                            originalList.set(refRealRegex.getPosition(), refRealRegex.getValue());
                        } else {
                            log.error("replaceAddRefRealRegex Incorrect sort request position add to the end " + refRealRegex);
                            originalList.add(refRealRegex.getValue());
                        }
                        count++;
                    } else {
                        int setPos = -1;
                        boolean replaced = false;
                        synchronized (originalList) {
                            for (int i = 0; i < originalList.size(); i++) {
                                if (refRealRegex.defaultMatch(originalList.get(i))) {
                                    setPos = i;
                                    break;
                                }
                            }
                        }
                        if (setPos > -1) {
                            originalList.set(setPos, refRealRegex.getValue());
                            replaced = true;
                            count++;
                        }
                        if (!replaced) {
                            log.error("replaceAddRefRealRegex add to the end " + refRealRegex);
                            originalList.add(refRealRegex.getValue());
                        }
                    }
                }
            }
        }
        return count;
    }

    public static Integer replaceRefRealRegex(List<IRealRegex> originalList, List<RefRealRegex> refList) {
        Integer count = 0;
        if ((refList != null) && (!refList.isEmpty())) {
            for (RefRealRegex refRealRegex : refList) {
                if (RefType.REPLACE.equals(refRealRegex.getType())) {
                    if (refRealRegex.getValue() == null) {
                        log.error("replaceRefRealRegex null value for ref(cannot process this param)\n" + refRealRegex);
                        continue;
                    }
                    if (originalList == null) {
                        log.warn("replaceRefRealRegex real regex list is null, ref not replaced (no more refs replaced)\n" + refRealRegex);
                        break;
                    }
                    if (refRealRegex.getPosition() != null) {
                        if ((refRealRegex.getPosition() >= 0) && (refRealRegex.getPosition() < originalList.size())) {
                            originalList.set(refRealRegex.getPosition(), refRealRegex.getValue());
                            count++;
                        } else {
                            log.error("replaceRefRealRegex Incorrect sort request position cannot replace " + refRealRegex);
                        }
                    } else {
                        int setPos = -1;
                        boolean replaced = false;
                        synchronized (originalList) {
                            for (int i = 0; i < originalList.size(); i++) {
                                if (refRealRegex.defaultMatch(originalList.get(i))) {
                                    setPos = i;
                                    break;
                                }
                            }
                        }

                        if (setPos > -1) {
                            originalList.set(setPos, refRealRegex.getValue());
                            replaced = true;
                            count++;
                        }
                        if (!replaced) {
                            log.error("replaceRefRealRegex not matched " + refRealRegex);
                        }
                    }
                }
            }
        }
        return count;
    }

    public static Integer removeRefRealRegex(List<IRealRegex> originalList, List<RefRealRegex> refList) {
        Integer count = 0;
        if ((refList != null) && (!refList.isEmpty())) {
            for (RefRealRegex refRealRegex : refList) {
                if (RefType.REMOVE.equals(refRealRegex.getType())) {
                    if (refRealRegex.getValue() == null) {
                        log.error("removeRefRealRegex null value for ref(cannot process this param)\n" + refRealRegex);
                        continue;
                    }
                    if (originalList == null) {
                        log.warn("removeRefRealRegex real regex list is null, ref not replaced (no more refs replaced)\n" + refRealRegex);
                        break;
                    }
                    if (refRealRegex.getPosition() != null) {
                        if ((refRealRegex.getPosition() >= 0) && (refRealRegex.getPosition() < originalList.size())) {
                            originalList.remove(refRealRegex.getPosition().intValue());
                            count++;
                        } else {
                            log.error("removeRefRealRegex Incorrect sort request position cannot remove " + refRealRegex);
                        }
                    } else {
                        int setPos = -1;
                        boolean replaced = false;
                        synchronized (originalList) {
                            for (int i = 0; i < originalList.size(); i++) {
                                if (refRealRegex.defaultMatch(originalList.get(i))) {
                                    setPos = i;
                                    break;
                                }
                            }
                        }

                        if (setPos > -1) {
                            originalList.remove(setPos);
                            replaced = true;
                            count++;
                        }
                        if (!replaced) {
                            log.error("removeRefRealRegex not matched " + refRealRegex);
                        }
                    }
                }
            }
        }
        return count;
    }

    public static Integer cpAddRefRealRegex(List<IRealRegex> originalList, List<RefRealRegex> refList) {
        Integer count = 0;
        if ((refList != null) && (!refList.isEmpty())) {
            IRealRegex resolvedObj = null;
            for (RefRealRegex refRealRegex : refList) {
                if (RefType.CP_ADD.equals(refRealRegex.getType())) {
                    if (originalList == null) {
                        log.warn("cpAddRefRealRegex real regex list is null, ref not replaced (no more refs replaced)\n" + refRealRegex);
                        break;
                    }
                    resolvedObj = null;
                    synchronized (originalList) {
                        for (int i = 0; i < originalList.size(); i++) {
                            if (refRealRegex.defaultMatch(originalList.get(i))) {
                                resolvedObj = refRealRegex.resolve(originalList.get(i));
                                if (resolvedObj != null) {
                                    break;
                                }
                            }
                        }
                    }
                    if (resolvedObj != null) {
                        if (refRealRegex.getPosition() != null) {
                            if ((refRealRegex.getPosition() >= 0) && (refRealRegex.getPosition() <= originalList.size())) {
                                originalList.add(refRealRegex.getPosition().intValue(), resolvedObj);
                            } else {
                                log.warn("cpAddRefRealRegex Incorrect ref real regex position (adding to the end) position "
                                        + refRealRegex.getPosition() + " size=" + originalList.size());
                                originalList.add(resolvedObj);
                            }
                        } else {
                            originalList.add(resolvedObj);
                        }
                        count++;
                    } else {
                        log.error("cpAddRefRealRegex resolved real regex is null for ref \n" + refRealRegex);
                    }
                }
            }
        }
        return count;
    }

    public static Integer cpReplaceRefRealRegex(List<IRealRegex> originalList, List<RefRealRegex> refList) {
        Integer count = 0;
        if ((refList != null) && (!refList.isEmpty())) {
            IRealRegex resolvedObj = null;
            for (RefRealRegex refRealRegex : refList) {
                if (RefType.CP_REPLACE.equals(refRealRegex.getType())) {
                    if (originalList == null) {
                        log.warn("cpReplaceRefRealRegex real regex list is null, ref not replaced (no more refs replaced)\n" + refRealRegex);
                        break;
                    }
                    resolvedObj = null;
                    int setPos = -1;
                    synchronized (originalList) {
                        for (int i = 0; i < originalList.size(); i++) {
                            if (refRealRegex.defaultMatch(originalList.get(i))) {
                                resolvedObj = refRealRegex.resolve(originalList.get(i));
                                if (resolvedObj != null) {
                                    setPos = i;
                                    break;
                                }
                            }
                        }
                    }
                    if (resolvedObj != null) {
                        if (refRealRegex.getPosition() != null) {
                            if ((refRealRegex.getPosition() >= 0) && (refRealRegex.getPosition() < originalList.size())) {
                                originalList.set(refRealRegex.getPosition().intValue(), resolvedObj);
                            } else {
                                log.warn("cpReplaceRefRealRegex Incorrect ref real regex position (no action) position "
                                        + refRealRegex.getPosition() + " size=" + originalList.size());
                            }
                        } else if (setPos > 0) {
                            originalList.set(setPos, resolvedObj);
                        } else {
                            log.error("cpReplaceRefRealRegex Unknown error setPos " + setPos + " size " + originalList.size()
                                    + " Ref\n" + refRealRegex);
                        }
                        count++;
                    } else {
                        log.error("cpReplaceRefRealRegex resolved real regex is null for ref \n" + refRealRegex);
                    }
                }
            }
        }
        return count;
    }

}