package org.eclipselabs.real.core.searchresult.sort;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipselabs.real.core.regex.IRealRegex;
import org.eclipselabs.real.core.searchresult.ISearchResult;
import org.eclipselabs.real.core.searchresult.resultobject.ISearchResultObject;
import org.eclipselabs.real.core.searchresult.resultobject.SROTextComparator;

public class RegexSortRequest extends InternalSortRequest implements IRegexSortRequest {

    private static final Logger log = LogManager.getLogger(RegexSortRequest.class);
    protected List<IRealRegex> sortRegexList;
    protected int regexFlags;

    public RegexSortRequest() {
        super(SortingType.REGEX);
    }

    public RegexSortRequest(String aName) {
        super(SortingType.REGEX, aName);
    }

    public RegexSortRequest(SortingApplicability appl, String aName) {
        super(SortingType.REGEX, appl, aName);
    }

    public RegexSortRequest(SortingApplicability appl, List<IRealRegex> sortList, String aName) {
        super(SortingType.REGEX, appl, aName);
        sortRegexList = sortList;
    }

    public RegexSortRequest(RegexSortRequest copyObj) {
        super(copyObj.getType(), copyObj.getSortApplicability(), copyObj.getName());
        if (copyObj.getSortRegexList() != null) {
            List<IRealRegex> newSortRegList = new ArrayList<>();
            for (IRealRegex currRealReg : copyObj.getSortRegexList()) {
                try {
                    newSortRegList.add(currRealReg.clone());
                } catch (CloneNotSupportedException e) {
                    log.error("Error while cloning RegexSortRequest",e);
                }
            }
            sortRegexList = new ArrayList<>(newSortRegList);
        }
        regexFlags = copyObj.getRegexFlags();
    }

    protected List<IRealRegex> getCloneRegexList() {
        List<IRealRegex> newSortRegList = null;
        if (sortRegexList != null) {
            newSortRegList = new ArrayList<>();
            for (IRealRegex currRealReg : sortRegexList) {
                try {
                    newSortRegList.add(currRealReg.clone());
                } catch (CloneNotSupportedException e) {
                    log.error("Error while cloning the RegexList for RegexSortRequest",e);
                }
            }
        }
        return newSortRegList;
    }

    public void changeSortRegexList(List<String> newOrder) {
        if ((newOrder != null) && (!newOrder.isEmpty()) && (sortRegexList != null) && (!sortRegexList.isEmpty())) {
            List<IRealRegex> newList = new ArrayList<>();
            for (String realRegName : newOrder) {
                for (IRealRegex realReg : sortRegexList) {
                    if (realReg.getRegexName().equals(realRegName)) {
                        newList.add(realReg);
                    }
                }
            }
            sortRegexList = newList;
        }
    }

    @Override
    public RegexSortRequest clone() throws CloneNotSupportedException {
        RegexSortRequest newInst = (RegexSortRequest)super.clone();
        newInst.setSortRegexList(getCloneRegexList());
        return newInst;
    }

    @Override
    public <R extends ISearchResult<O>, O extends ISearchResultObject> Comparator<O> getComparator(R searchRes) {
        Comparator<O> resComp = null;
        if ((searchRes != null) && (searchRes.getSRObjects() != null) && (!searchRes.getSRObjects().isEmpty())) {
            resComp = new SROTextComparator<O>(sortRegexList, searchRes.getCachedReplaceTable(), regexFlags);
        }
        return resComp;
    }

    @Override
    public <R extends ISearchResult<O>, O extends ISearchResultObject> void sortResultObjects(R searchRes) {
        if (sortAvailable()) {
            if ((searchRes != null) && (searchRes.getSRObjects() != null) && (!searchRes.getSRObjects().isEmpty())) {
                Collections.sort(searchRes.getSRObjects(), getComparator(searchRes));
            } else {
                log.warn("sort Null search result or no result objects");
            }
        } else {
            log.debug("sortResultObjects This internal sort request" + this + " doesn't have "
                    + SortingApplicability.ALL + " in the scope. No sort performed");
        }
    }

    @Override
    public List<IRealRegex> getSortRegexList() {
        return sortRegexList;
    }

    @Override
    public void setSortRegexList(List<IRealRegex> sortRegexList) {
        this.sortRegexList = sortRegexList;
    }

    @Override
    public int getRegexFlags() {
        return regexFlags;
    }

    @Override
    public void setRegexFlags(int regexFlags) {
        this.regexFlags = regexFlags;
    }

    @Override
    public String toString() {
        return "RegexSortRequest [name=" + name + ", type=" + type + ", sortApplicability=" + sortApplicability
                + ", sortRegexList=" + sortRegexList + ", regexFlags=" + regexFlags + "]";
    }


}
