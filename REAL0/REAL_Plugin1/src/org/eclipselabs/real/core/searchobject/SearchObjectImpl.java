package org.eclipselabs.real.core.searchobject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipselabs.real.core.searchobject.crit.IAcceptanceCriterion;
import org.eclipselabs.real.core.searchobject.param.IReplaceableParam;
import org.eclipselabs.real.core.searchobject.param.ReplaceableParamKey;
import org.eclipselabs.real.core.searchresult.ISearchResult;
import org.eclipselabs.real.core.searchresult.resultobject.ISearchResultObject;
import org.eclipselabs.real.core.searchresult.sort.IInternalSortRequest;
import org.eclipselabs.real.core.searchresult.sort.SortingType;

public abstract class SearchObjectImpl<R extends ISearchResult<O>,O extends ISearchResultObject>
        implements ISearchObject<R,O> {
    private static final Logger log = LogManager.getLogger(SearchObjectImpl.class);
    protected String searchObjectName;
    protected String searchObjectDescription;
    protected SearchObjectType theType;
    protected Integer regexFlags;
    protected List<IInternalSortRequest> sortRequestList = Collections.synchronizedList(new ArrayList<IInternalSortRequest>());
    protected List<IAcceptanceCriterion> acceptanceList = Collections.synchronizedList(new ArrayList<IAcceptanceCriterion>());
    protected List<IReplaceableParam<?>> replaceableParams = Collections.synchronizedList(new ArrayList<IReplaceableParam<?>>());

    public SearchObjectImpl(SearchObjectType aType, String aName) {
        theType = aType;
        searchObjectName = aName;
    }

    @Override
    public SearchObjectType getType() {
        return theType;
    }

    @Override
    public String getSearchObjectName() {
        return searchObjectName;
    }

    @Override
    public void setSearchObjectName(String newName) {
        searchObjectName = newName;
    }


    @Override
    public String getSearchObjectDescription() {
        return searchObjectDescription;
    }

    @Override
    public void setSearchObjectDescription(String newDescription) {
        searchObjectDescription = newDescription;
    }

    @Override
    public void addParam(IReplaceableParam<?> newParam) {
        synchronized(replaceableParams) {
            // before adding a new param remove all params with the same name
            List<IReplaceableParam<?>> sameNameparams = replaceableParams.stream()
                    .filter(param -> param.getKey().equals(newParam.getKey()))
                    .collect(Collectors.toList());
            replaceableParams.removeAll(sameNameparams);
            replaceableParams.add(newParam);
        }
    }

    @Override
    public Optional<IReplaceableParam<?>> getParam(ReplaceableParamKey key) {
        Optional<IReplaceableParam<?>> result = Optional.empty();
        synchronized(replaceableParams) {
            result = replaceableParams.stream().filter((rp) -> key.equals(rp.getKey())).findFirst();
        }
        return result;
    }

    @Override
    public boolean removeParam(ReplaceableParamKey key) {
        boolean result = false;
        synchronized(replaceableParams) {
            List<IReplaceableParam<?>> toRemove = replaceableParams.stream().filter((rp) -> key.equals(rp.getKey())).collect(Collectors.toList());
            result = !toRemove.isEmpty();
            replaceableParams.removeAll(toRemove);
        }
        return result;
    }

    @Override
    public boolean paramExists(ReplaceableParamKey key) {
        return replaceableParams.stream().anyMatch((rp) -> key.equals(rp.getKey()));
    }

    /**
     * This method does not allow for setting the collection reference.
     * It clears the internal collection and adds the new replaceable params
     * @param replaceParams
     */
    public void setReplaceableParams(List<IReplaceableParam<?>> replaceParams) {
        this.replaceableParams.clear();
        this.replaceableParams.addAll(replaceParams);
    }

    @Override
    public List<IReplaceableParam<?>> getCloneParamList() {
        List<IReplaceableParam<?>> actualParams = replaceableParams;
        List<IReplaceableParam<?>> cloneList = new ArrayList<>();
        for (IReplaceableParam<?> rp : actualParams) {
            try {
                cloneList.add(rp.clone());
            } catch (CloneNotSupportedException e) {
                log.error("getCloneParamList ", e);
            }
        }
        return cloneList;
    }

    @Override
    public ISearchProgressMonitor getSearchProgressMonitor() {
        return new SearchProgressMonitorImpl();
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
    public List<IInternalSortRequest> getSortRequestList() {
        return sortRequestList;
    }

    @Override
    public void setSortRequestList(List<IInternalSortRequest> sortRequestList) {
        this.sortRequestList = sortRequestList;
    }

    public List<IInternalSortRequest> getCloneSortRequestList () {
        List<IInternalSortRequest> cloneLst = null;
        if (sortRequestList != null) {
            cloneLst = new ArrayList<>();
            for (IInternalSortRequest currIntSortReq : sortRequestList) {
                try {
                    cloneLst.add(currIntSortReq.clone());
                } catch (CloneNotSupportedException e) {
                    log.error("Clone not suported",e);
                }
            }
        }
        return cloneLst;
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
    public List<IAcceptanceCriterion> getAcceptanceList(Predicate<IAcceptanceCriterion> stagePred) {
        List<IAcceptanceCriterion> result = null;
        if (acceptanceList != null) {
            if (stagePred != null) {
                result = new ArrayList<>();
                synchronized (acceptanceList) {
                    for (IAcceptanceCriterion ac : acceptanceList) {
                        if (stagePred.test(ac)) {
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
    public List<IAcceptanceCriterion> getCloneAcceptanceList(Predicate<IAcceptanceCriterion> stagePred) {
        List<IAcceptanceCriterion> result = null;
        if (acceptanceList != null) {
            if (stagePred != null) {
                result = new ArrayList<>();
                synchronized (acceptanceList) {
                    for (IAcceptanceCriterion ac : acceptanceList) {
                        if (stagePred.test(ac)) {
                            try {
                                result.add(ac.clone());
                            } catch (CloneNotSupportedException e) {
                                log.error("getCloneAcceptanceList ", e);
                            }
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
    public boolean isSortingAvailable(SortingType requestedType) {
        boolean res = false;
        if ((requestedType != null) && (sortRequestList != null) && (!sortRequestList.isEmpty())) {
            for (IInternalSortRequest currReq : sortRequestList) {
                if (currReq.getType().equals(requestedType)) {
                    res = true;
                    break;
                }
            }
        } else if ((requestedType == null) && (sortRequestList != null) && (!sortRequestList.isEmpty())) {
            res = true;
        }
        return res;
    }

    @Override
    public ISearchObject<R,O> clone() throws CloneNotSupportedException {
        SearchObjectImpl<R,O> cloneObj = (SearchObjectImpl<R,O>)super.clone();
        // sort requests
        cloneObj.setSortRequestList(getCloneSortRequestList());
        //acceptances
        if (acceptanceList != null) {
            List<IAcceptanceCriterion> clonedList = Collections.synchronizedList(new ArrayList<IAcceptanceCriterion>());
            for (IAcceptanceCriterion currCriterion : acceptanceList) {
                clonedList.add(currCriterion.clone());
            }
            cloneObj.setAcceptanceList(clonedList);
        }
        //replace params
        cloneObj.replaceableParams = getCloneParamList();
        return cloneObj;
    }


}
